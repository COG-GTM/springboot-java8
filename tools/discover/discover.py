#!/usr/bin/env python3
"""Agentless, tag-scoped discovery of the application dependency graph on AWS.

Walks ELBv2, target groups, EC2, security groups, Route53, Secrets Manager, RDS and
ECR for every resource tagged Project=<project> and emits:

  <out>/dependency-graph.json   nodes {id,type,arn,attrs} + edges {from,to,type,attrs}
  <out>/dependency-graph.md     Mermaid rendering
  <out>/dependency-graph.dot    Graphviz source
  <out>/dependency-graph.png    rendered with `dot` when available

Edge types: routes_to, health_checks, allows_ingress, resolves_to, reads_secret,
connects_to_db, runs_image.

Only read-only Describe*/List*/Get* calls are made. Secret values are fetched solely to
enumerate their JSON key names (the contract) and are discarded, never written.
"""
import argparse
import base64
import json
import os
import re
import shutil
import subprocess
import sys

import boto3

PROJECT_TAG = "Project"
TRACK_TAG = "Track"


class Graph:
    def __init__(self):
        self.nodes = {}
        self.edges = []

    def node(self, nid, ntype, arn=None, **attrs):
        n = self.nodes.setdefault(nid, {"id": nid, "type": ntype, "arn": arn, "attrs": {}})
        if arn:
            n["arn"] = arn
        n["attrs"].update({k: v for k, v in attrs.items() if v is not None})
        return n

    def edge(self, src, dst, etype, **attrs):
        e = {"from": src, "to": dst, "type": etype, "attrs": {k: v for k, v in attrs.items() if v is not None}}
        if e not in self.edges:
            self.edges.append(e)


def tags(lst):
    return {t["Key"]: t["Value"] for t in (lst or [])}


def has_project(tag_map, project, track=None):
    return tag_map.get(PROJECT_TAG) == project and (track is None or tag_map.get(TRACK_TAG) == track)


def tag_filters(project, track):
    f = [{"Name": f"tag:{PROJECT_TAG}", "Values": [project]}]
    if track:
        f.append({"Name": f"tag:{TRACK_TAG}", "Values": [track]})
    return f


def discover(region, project, track=None):
    s = boto3.session.Session(region_name=region)
    g = Graph()

    # ---- Security groups (the firewall)
    ec2 = s.client("ec2")
    sgs = ec2.describe_security_groups(Filters=tag_filters(project, track))["SecurityGroups"]
    sg_by_id = {}
    for sg in sgs:
        nid = f"sg:{sg['GroupName']}"
        sg_by_id[sg["GroupId"]] = nid
        rules = []
        for p in sg["IpPermissions"]:
            ports = "all" if p.get("IpProtocol") == "-1" else f"{p.get('FromPort')}-{p.get('ToPort')}" if p.get("FromPort") != p.get("ToPort") else str(p.get("FromPort"))
            for r in p.get("IpRanges", []):
                rules.append({"port": ports, "from": r["CidrIp"]})
            for r in p.get("UserIdGroupPairs", []):
                rules.append({"port": ports, "from": r["GroupId"]})
        g.node(nid, "security_group", arn=f"arn:aws:ec2:{region}::security-group/{sg['GroupId']}",
               group_id=sg["GroupId"], ingress=rules, description=sg.get("Description"))
    for sg in sgs:
        for p in sg["IpPermissions"]:
            port = p.get("FromPort")
            for r in p.get("UserIdGroupPairs", []):
                if r["GroupId"] in sg_by_id:
                    g.edge(sg_by_id[r["GroupId"]], sg_by_id[sg["GroupId"]], "allows_ingress", port=port, protocol=p.get("IpProtocol"))
            for r in p.get("IpRanges", []):
                g.node(f"cidr:{r['CidrIp']}", "cidr")
                g.edge(f"cidr:{r['CidrIp']}", sg_by_id[sg["GroupId"]], "allows_ingress", port=port, protocol=p.get("IpProtocol"))

    # ---- EC2 instances
    insts = ec2.describe_instances(Filters=tag_filters(project, track) +
                                           [{"Name": "instance-state-name", "Values": ["pending", "running", "stopping", "stopped"]}])
    inst_by_id = {}
    for res in insts["Reservations"]:
        for i in res["Instances"]:
            t = tags(i.get("Tags"))
            nid = f"ec2:{t.get('Name', i['InstanceId'])}"
            inst_by_id[i["InstanceId"]] = nid
            g.node(nid, "ec2_instance", arn=f"arn:aws:ec2:{region}::instance/{i['InstanceId']}",
                   instance_id=i["InstanceId"], instance_type=i["InstanceType"], private_ip=i.get("PrivateIpAddress"),
                   state=i["State"]["Name"], runtime_image=t.get("RuntimeImage"), app_image=t.get("AppImage"))
            for sg in i.get("SecurityGroups", []):
                if sg["GroupId"] in sg_by_id:
                    g.edge(sg_by_id[sg["GroupId"]], nid, "allows_ingress", scope="attached")
            if t.get("AppImage"):
                img = t["AppImage"]
                g.node(f"image:{img.split('/')[-1]}", "container_image", uri=img, base_image=t.get("RuntimeImage"))
                g.edge(nid, f"image:{img.split('/')[-1]}", "runs_image", base_image=t.get("RuntimeImage"))
            # user-data is where the app's env contract lives (which secret key, which DB host)
            try:
                ud = ec2.describe_instance_attribute(InstanceId=i["InstanceId"], Attribute="userData")["UserData"].get("Value")
                if ud:
                    text = base64.b64decode(ud).decode("utf-8", "replace")
                    key = None
                    for line in text.splitlines():
                        if "DB_PASSWORD=" in line and "jq -r" in line:
                            mm = re.search(r"jq -r '\.(\w+)'", line)
                            key = mm.group(1) if mm else None
                    dbh = re.search(r"jdbc:postgresql://([^:/]+)", text)
                    ports = re.search(r"-p\s+(\d+):(\d+)", text)
                    g.nodes[nid]["attrs"].update({
                        "secret_key_read": key,
                        "db_host": dbh.group(1) if dbh else None,
                        "host_port": int(ports.group(1)) if ports else None,
                        "container_port": int(ports.group(2)) if ports else None,
                    })
            except Exception as exc:  # noqa: BLE001
                print(f"warn: user-data for {i['InstanceId']}: {exc}", file=sys.stderr)

    # ---- ELBv2 + target groups
    elb = s.client("elbv2")
    lbs = elb.describe_load_balancers()["LoadBalancers"]
    lb_arns = [lb["LoadBalancerArn"] for lb in lbs]
    lb_tags = {}
    for i in range(0, len(lb_arns), 20):
        for d in elb.describe_tags(ResourceArns=lb_arns[i:i + 20])["TagDescriptions"]:
            lb_tags[d["ResourceArn"]] = tags(d["Tags"])
    tg_by_arn = {}
    for lb in lbs:
        if not has_project(lb_tags.get(lb["LoadBalancerArn"], {}), project, track):
            continue
        lb_id = f"alb:{lb['LoadBalancerName']}"
        g.node(lb_id, "load_balancer", arn=lb["LoadBalancerArn"], dns_name=lb["DNSName"], scheme=lb["Scheme"], state=lb["State"]["Code"])
        for sgid in lb.get("SecurityGroups", []):
            if sgid in sg_by_id:
                g.edge(sg_by_id[sgid], lb_id, "allows_ingress", scope="attached")
        for lst in elb.describe_listeners(LoadBalancerArn=lb["LoadBalancerArn"])["Listeners"]:
            for a in lst["DefaultActions"]:
                if a["Type"] != "forward":
                    continue
                tg_arn = a["TargetGroupArn"]
                tg = elb.describe_target_groups(TargetGroupArns=[tg_arn])["TargetGroups"][0]
                tg_id = f"tg:{tg['TargetGroupName']}"
                tg_by_arn[tg_arn] = tg_id
                g.node(tg_id, "target_group", arn=tg_arn, port=tg["Port"], protocol=tg["Protocol"],
                       health_check_path=tg.get("HealthCheckPath"), health_check_port=tg.get("HealthCheckPort"),
                       matcher=tg.get("Matcher", {}).get("HttpCode"))
                g.edge(lb_id, tg_id, "routes_to", listener_port=lst["Port"], protocol=lst["Protocol"])
                for th in elb.describe_target_health(TargetGroupArn=tg_arn)["TargetHealthDescriptions"]:
                    tid = th["Target"]["Id"]
                    if tid in inst_by_id:
                        g.edge(tg_id, inst_by_id[tid], "routes_to", port=th["Target"].get("Port"))
                        g.edge(tg_id, inst_by_id[tid], "health_checks", path=tg.get("HealthCheckPath"),
                               port=th["Target"].get("Port") if tg.get("HealthCheckPort") == "traffic-port" else tg.get("HealthCheckPort"),
                               state=th["TargetHealth"]["State"], reason=th["TargetHealth"].get("Reason"))

    # ---- RDS
    rds = s.client("rds")
    db_by_addr = {}
    for db in rds.describe_db_instances()["DBInstances"]:
        if not has_project(tags(db.get("TagList")), project, track):
            continue
        nid = f"rds:{db['DBInstanceIdentifier']}"
        addr = db.get("Endpoint", {}).get("Address")
        db_by_addr[addr] = nid
        g.node(nid, "rds_instance", arn=db["DBInstanceArn"], engine=db["Engine"], engine_version=db["EngineVersion"],
               endpoint=addr, port=db.get("Endpoint", {}).get("Port"), db_name=db.get("DBName"), status=db["DBInstanceStatus"],
               instance_class=db["DBInstanceClass"])
        for sg in db.get("VpcSecurityGroups", []):
            if sg["VpcSecurityGroupId"] in sg_by_id:
                g.edge(sg_by_id[sg["VpcSecurityGroupId"]], nid, "allows_ingress", scope="attached")

    # ---- Secrets Manager (names + key names only; values are never fetched)
    sm = s.client("secretsmanager")
    for sec in sm.list_secrets(Filters=[{"Key": "tag-key", "Values": [PROJECT_TAG]}])["SecretList"]:
        if not has_project(tags(sec.get("Tags")), project, track):
            continue
        nid = f"secret:{sec['Name']}"
        keys = None
        try:
            # key names are part of the contract; values are discarded immediately
            val = sm.get_secret_value(SecretId=sec["ARN"])["SecretString"]
            keys = sorted(json.loads(val).keys())
            del val
        except Exception:  # noqa: BLE001
            keys = None
        g.node(nid, "secret", arn=sec["ARN"], keys=keys)
        for iid, inode in inst_by_id.items():
            g.edge(inode, nid, "reads_secret", key=g.nodes[inode]["attrs"].get("secret_key_read"))

    # ---- Route53 (private zones)
    r53 = s.client("route53")
    for z in r53.list_hosted_zones()["HostedZones"]:
        ztags = tags(r53.list_tags_for_resource(ResourceType="hostedzone", ResourceId=z["Id"].split("/")[-1])["ResourceTagSet"]["Tags"])
        if not has_project(ztags, project, track):
            continue
        zid = f"zone:{z['Name'].rstrip('.')}"
        g.node(zid, "hosted_zone", arn=f"arn:aws:route53:::hostedzone/{z['Id'].split('/')[-1]}", private=z["Config"]["PrivateZone"])
        for rr in r53.list_resource_record_sets(HostedZoneId=z["Id"])["ResourceRecordSets"]:
            if rr["Type"] not in ("A", "CNAME"):
                continue
            name = rr["Name"].rstrip(".")
            rid = f"dns:{name}"
            target = rr["AliasTarget"]["DNSName"].rstrip(".") if "AliasTarget" in rr else rr["ResourceRecords"][0]["Value"]
            g.node(rid, "dns_record", zone=zid, record_type=rr["Type"], target=target)
            g.edge(zid, rid, "resolves_to", scope="contains")
            for n in list(g.nodes.values()):
                a = n["attrs"]
                if n["type"] == "load_balancer" and a.get("dns_name", "").lower() == target.lower():
                    g.edge(rid, n["id"], "resolves_to")
                if n["type"] == "rds_instance" and a.get("endpoint") == target:
                    g.edge(rid, n["id"], "resolves_to")
                if n["type"] == "ec2_instance" and a.get("private_ip") == target:
                    g.edge(rid, n["id"], "resolves_to")

    # ---- connects_to_db: instance user-data names a DB host; resolve through DNS
    for iid, inode in inst_by_id.items():
        host = g.nodes[inode]["attrs"].get("db_host")
        if not host:
            continue
        rid = f"dns:{host}"
        if rid in g.nodes:
            g.edge(inode, rid, "connects_to_db", host=host, port=5432)
            for e in list(g.edges):
                if e["from"] == rid and e["type"] == "resolves_to" and e["to"].startswith("rds:"):
                    g.edge(inode, e["to"], "connects_to_db", via=host, port=5432)

    # ---- ECR
    ecr = s.client("ecr")
    for repo in ecr.describe_repositories()["repositories"]:
        rtags = tags(ecr.list_tags_for_resource(resourceArn=repo["repositoryArn"])["tags"])
        if not has_project(rtags, project, track):
            continue
        imgs = ecr.describe_images(repositoryName=repo["repositoryName"]).get("imageDetails", [])
        for img in imgs:
            for t in img.get("imageTags", []):
                nid = f"image:{repo['repositoryName'].split('/')[-1]}:{t}"
                g.node(nid, "container_image", arn=repo["repositoryArn"], uri=f"{repo['repositoryUri']}:{t}",
                       pushed_at=img["imagePushedAt"].isoformat(), digest=img["imageDigest"])
    return g


SHAPES = {
    "load_balancer": ("ALB", "([{label}])"), "target_group": ("TG", "[/{label}/]"), "ec2_instance": ("EC2", "[{label}]"),
    "security_group": ("SG", "{{{{{label}}}}}"), "rds_instance": ("RDS", "[({label})]"), "secret": ("Secret", "[[{label}]]"),
    "hosted_zone": ("Zone", "(({label}))"), "dns_record": ("DNS", "({label})"), "container_image": ("Image", ">{label}]"), "cidr": ("CIDR", "({label})"),
}


def mermaid(g):
    out = ["```mermaid", "graph LR"]
    ids = {nid: f"n{i}" for i, nid in enumerate(g.nodes)}
    for nid, n in g.nodes.items():
        kind, shape = SHAPES.get(n["type"], (n["type"], "[{label}]"))
        label = f"{kind}<br/>{nid.split(':', 1)[1]}".replace('"', "'")
        quoted = '"' + label + '"'
        out.append(f'  {ids[nid]}{shape.format(label=quoted)}')
    for e in g.edges:
        if e["attrs"].get("scope") in ("attached", "contains"):
            out.append(f"  {ids[e['from']]} -.- {ids[e['to']]}")
            continue
        extra = ", ".join(f"{k}={v}" for k, v in e["attrs"].items() if k in ("port", "path", "key", "listener_port"))
        out.append(f"  {ids[e['from']]} -->|{e['type']}{(' ' + extra) if extra else ''}| {ids[e['to']]}")
    out.append("```")
    return "\n".join(out)


COLORS = {"load_balancer": "#f9d67a", "target_group": "#fbe7b3", "ec2_instance": "#a8d5ba", "security_group": "#f4a6a6",
          "rds_instance": "#9ec5fe", "secret": "#d3b7f7", "hosted_zone": "#cfd8dc", "dns_record": "#e0e7ea", "container_image": "#c8e6c9", "cidr": "#eeeeee"}


def dot(g):
    out = ["digraph deps {", '  rankdir=LR; node [shape=box, style="rounded,filled", fontname=Helvetica, fontsize=10]; edge [fontname=Helvetica, fontsize=9];']
    for nid, n in g.nodes.items():
        kind = SHAPES.get(n["type"], (n["type"],))[0]
        a = n["attrs"]
        detail = ""
        if n["type"] == "target_group":
            detail = f"\\n:{a.get('port')} hc={a.get('health_check_path')}"
        elif n["type"] == "security_group":
            detail = "".join(f"\\n{r['port']} from {r['from']}" for r in a.get("ingress", []))
        elif n["type"] == "ec2_instance":
            detail = f"\\n{a.get('instance_type')} :{a.get('host_port')} base={a.get('runtime_image')}"
        elif n["type"] == "secret":
            detail = f"\\nkeys={a.get('keys')}"
        elif n["type"] == "rds_instance":
            detail = f"\\n{a.get('engine')} {a.get('engine_version')} :{a.get('port')}"
        elif n["type"] == "dns_record":
            detail = f"\\n{a.get('record_type')}"
        label = f"{kind}: {nid.split(':', 1)[1]}{detail}".replace('"', "'")
        out.append(f'  "{nid}" [label="{label}", fillcolor="{COLORS.get(n["type"], "#ffffff")}"];')
    for e in g.edges:
        if e["attrs"].get("scope") in ("attached", "contains"):
            out.append(f'  "{e["from"]}" -> "{e["to"]}" [style=dashed, color=gray, arrowhead=none];')
            continue
        extra = " ".join(f"{k}={v}" for k, v in e["attrs"].items() if k in ("port", "path", "key", "listener_port", "state"))
        out.append(f'  "{e["from"]}" -> "{e["to"]}" [label="{e["type"]}{(chr(10) + extra) if extra else ""}"];')
    out.append("}")
    return "\n".join(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--region", default=os.environ.get("AWS_REGION", "us-east-1"))
    ap.add_argument("--project", default="modernization-demo")
    ap.add_argument("--track", default=None, help="also require Track=<value> (java|dotnet)")
    ap.add_argument("--out", default="demo")
    args = ap.parse_args()

    g = discover(args.region, args.project, args.track)
    os.makedirs(args.out, exist_ok=True)
    graph = {"region": args.region, "project": args.project, "track": args.track, "nodes": list(g.nodes.values()), "edges": g.edges}
    with open(os.path.join(args.out, "dependency-graph.json"), "w") as f:
        json.dump(graph, f, indent=2, default=str)
    md = ["# Application dependency graph", "",
          f"Discovered agentlessly from AWS APIs in `{args.region}` for `Project={args.project}`{f', `Track={args.track}`' if args.track else ''} "
          f"({len(g.nodes)} nodes, {len(g.edges)} edges). Regenerate with `make aws-discover`.", "",
          "![dependency graph](dependency-graph.png)", "", mermaid(g), "", "## Edges", "", "| from | edge | to | attrs |", "|---|---|---|---|"]
    for e in g.edges:
        if e["attrs"].get("scope") in ("attached", "contains"):
            continue
        md.append(f"| `{e['from']}` | {e['type']} | `{e['to']}` | {json.dumps(e['attrs'], default=str)} |")
    with open(os.path.join(args.out, "dependency-graph.md"), "w") as f:
        f.write("\n".join(md) + "\n")
    dot_path = os.path.join(args.out, "dependency-graph.dot")
    with open(dot_path, "w") as f:
        f.write(dot(g))
    if shutil.which("dot"):
        subprocess.run(["dot", "-Tpng", dot_path, "-o", os.path.join(args.out, "dependency-graph.png")], check=True)
    else:
        print("warn: graphviz `dot` not found; PNG not rendered", file=sys.stderr)
    print(f"{len(g.nodes)} nodes, {len(g.edges)} edges -> {args.out}/dependency-graph.{{json,md,dot,png}}")


if __name__ == "__main__":
    main()
