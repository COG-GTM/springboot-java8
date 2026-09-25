#!/usr/bin/env python3
"""Impact matrix: live infrastructure (dependency-graph.json) vs. what the code declares.

Reads the checked-out repo (any branch/worktree via --repo) and compares the runtime
contract the code now expects with what the discovered infrastructure provides:

  health check path + port   <- application.properties (management.*) vs. target group
  admitted port              <- server/management port vs. app security-group ingress
  secret key name            <- spring.datasource.password=${KEY} vs. Secrets Manager keys
  base image / JDK           <- Dockerfile ARG + pom.xml java.version vs. EC2 RuntimeImage tag
  DB driver / dialect        <- pom.xml postgresql version, javax/jakarta, ddl-auto vs. RDS engine
  DNS                        <- DB host in the code vs. Route53 records

Exit 1 when drift is found (use --no-fail to always exit 0). Markdown goes to stdout
and, with --out, to a file so it can be pasted into demo/PLANTED-GAPS.md.
"""
import argparse
import json
import os
import re
import sys


def read(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def props(text):
    out = {}
    for line in text.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


def placeholder_key(value):
    m = re.match(r"\$\{([A-Za-z0-9_.-]+)(?::[^}]*)?\}", value or "")
    return m.group(1) if m else None


def declared(repo):
    p = props(read(os.path.join(repo, "src/main/resources/application.properties")))
    pom = read(os.path.join(repo, "pom.xml"))
    dockerfile = read(os.path.join(repo, "Dockerfile"))
    entity_dir = os.path.join(repo, "src/main/java/hello/model")
    entity_src = "".join(read(os.path.join(entity_dir, f)) for f in os.listdir(entity_dir) if f.endswith(".java"))

    server_port = int(p.get("server.port", "8080"))
    mgmt_port = int(p.get("management.server.port", server_port))
    base_path = p.get("management.endpoints.web.base-path", "/actuator")
    boot = re.search(r"<artifactId>spring-boot-starter-parent</artifactId>\s*<version>([^<]+)</version>", pom)
    java = re.search(r"<java\.version>([^<]+)</java\.version>", pom)
    pg = re.search(r"<artifactId>postgresql</artifactId>\s*(?:<version>([^<]+)</version>)?", pom)
    base_img = re.search(r"^ARG\s+BASE_IMAGE=(\S+)", dockerfile, re.M)
    db_url = p.get("spring.datasource.url", "")
    db_host = re.search(r"jdbc:postgresql://([^:/]+)", db_url)
    return {
        "spring_boot": boot.group(1) if boot else None,
        "java_version": java.group(1) if java else None,
        "persistence_api": "jakarta.persistence" if "jakarta.persistence" in entity_src else "javax.persistence",
        "postgres_driver": (pg.group(1) if pg and pg.group(1) else "managed-by-boot") if pg else None,
        "dockerfile_base_image": base_img.group(1) if base_img else None,
        "server_port": server_port,
        "management_port": mgmt_port,
        "health_path": f"{base_path.rstrip('/')}/health/readiness",
        "metrics_path": f"{base_path.rstrip('/')}/prometheus",
        "db_password_key": placeholder_key(p.get("spring.datasource.password")),
        "db_url_key": placeholder_key(db_url),
        "db_host_default": db_host.group(1) if db_host else None,
        "ddl_auto": p.get("spring.jpa.hibernate.ddl-auto"),
        "hikari_max_pool": p.get("spring.datasource.hikari.maximum-pool-size", "10 (framework default)"),
        "open_in_view": p.get("spring.jpa.open-in-view", "true (framework default)"),
        "sql_logging": p.get("spring.jpa.show-sql", "false"),
    }


def jdk_major(image):
    m = re.search(r"temurin:(\d+)", image or "")
    return int(m.group(1)) if m else None


def java_major(v):
    if not v:
        return None
    v = v.strip()
    return 8 if v in ("1.8", "8") else int(v)


def observed(graph):
    nodes = {n["id"]: n for n in graph["nodes"]}
    edges = graph["edges"]
    obs = {"target_groups": [], "app_sg_ports": [], "secret_keys": {}, "ec2": [], "rds": [], "dns": []}
    for n in nodes.values():
        a = n["attrs"]
        if n["type"] == "target_group":
            obs["target_groups"].append({"id": n["id"], "port": a.get("port"), "health_check_path": a.get("health_check_path"),
                                         "health_check_port": a.get("health_check_port"), "matcher": a.get("matcher")})
        elif n["type"] == "secret":
            obs["secret_keys"][n["id"]] = a.get("keys") or []
        elif n["type"] == "ec2_instance":
            obs["ec2"].append({"id": n["id"], "runtime_image": a.get("runtime_image"), "host_port": a.get("host_port"),
                               "secret_key_read": a.get("secret_key_read"), "db_host": a.get("db_host")})
        elif n["type"] == "rds_instance":
            obs["rds"].append({"id": n["id"], "engine": a.get("engine"), "engine_version": a.get("engine_version"), "port": a.get("port")})
        elif n["type"] == "dns_record":
            obs["dns"].append(n["id"].split(":", 1)[1])
    # ports the ALB is allowed to reach on the instance(s)
    for e in edges:
        if e["type"] == "allows_ingress" and e["from"].startswith("sg:") and "alb" in e["from"] and e["attrs"].get("port"):
            obs["app_sg_ports"].append(e["attrs"]["port"])
    health_states = [e["attrs"].get("state") for e in edges if e["type"] == "health_checks"]
    obs["health_states"] = health_states
    return obs


def matrix(dec, obs):
    rows = []  # (component, infra expects, code declares, status, impact)

    def row(component, infra, code, ok, impact, gap=None):
        rows.append({"component": component, "infra": infra, "code": code, "status": "OK" if ok else "DRIFT", "impact": impact, "gap": gap})

    # 1. runtime / base image
    for i in obs["ec2"]:
        infra_jdk = jdk_major(i["runtime_image"])
        code_jdk = java_major(dec["java_version"])
        ok = infra_jdk is not None and code_jdk is not None and infra_jdk >= code_jdk
        row(f"{i['id']} base image", f"{i['runtime_image']} (JDK {infra_jdk})",
            f"java.version={dec['java_version']}, Dockerfile ARG BASE_IMAGE={dec['dockerfile_base_image']}, Boot {dec['spring_boot']}",
            ok, "container exits with UnsupportedClassVersionError; target never healthy", gap=1)

    # 2. health check
    for tg in obs["target_groups"]:
        hc_port = tg["port"] if tg["health_check_port"] == "traffic-port" else int(tg["health_check_port"])
        ok = tg["health_check_path"] == dec["health_path"] and hc_port == dec["management_port"]
        row(f"{tg['id']} health check", f"GET :{hc_port}{tg['health_check_path']} -> {tg['matcher']}",
            f"GET :{dec['management_port']}{dec['health_path']}", ok,
            "ALB marks target unhealthy (404/timeout); all traffic 503", gap=2)

    # 3. secret key name
    for sid, keys in obs["secret_keys"].items():
        reader = next((i["secret_key_read"] for i in obs["ec2"]), None)
        ok = dec["db_password_key"] in keys and (reader is None or reader == dec["db_password_key"])
        row(f"{sid} key", f"keys={keys}; host reads .{reader}", f"spring.datasource.password=${{{dec['db_password_key']}}}", ok,
            "password resolves empty; Postgres rejects auth; app fails to start", gap=3)

    # 4. DB dependency
    for db in obs["rds"]:
        legacy_contract = dec["ddl_auto"] == "validate"
        row(f"{db['id']} schema contract", f"{db['engine']} {db['engine_version']} owned by infra/db/init (ddl-auto=validate expected)",
            f"ddl-auto={dec['ddl_auto']}, {dec['persistence_api']}, driver={dec['postgres_driver']}", legacy_contract,
            "app mutates a schema shared with the reporting consumer (view/function break or NOT NULL violations)", gap=4)

    # 5. firewall / DNS
    ports_needed = sorted({dec["server_port"], dec["management_port"]})
    for p in ports_needed:
        ok = p in obs["app_sg_ports"]
        row(f"app SG ingress :{p}", f"ALB -> app allowed on {sorted(set(obs['app_sg_ports']))}", f"app listens on {ports_needed}", ok,
            "health/metrics port silently dropped by the security group", gap=5)
    if dec["db_host_default"]:
        ok = dec["db_host_default"] in obs["dns"] or any(i["db_host"] for i in obs["ec2"])
        row("DB DNS name", f"records={obs['dns']}", f"default DB host {dec['db_host_default']} (overridable by DB_URL)", ok,
            "name does not resolve inside the VPC", gap=5)

    # 6. NFR-relevant defaults (not observable statically on infra; flagged for the load test)
    nfr_ok = str(dec["hikari_max_pool"]).startswith("20") and str(dec["open_in_view"]).startswith("false") and dec["sql_logging"] == "false"
    row("connection pool / logging", "baseline: pool=20, open-in-view=false, no SQL logging",
        f"pool={dec['hikari_max_pool']}, open-in-view={dec['open_in_view']}, show-sql={dec['sql_logging']}", nfr_ok,
        "latency/throughput regression under load only -> compare k6 vs demo/BASELINE.json", gap=6)
    return rows


def render(rows, dec, graph):
    scope = f"`Project={graph['project']}`" + (f", `Track={graph['track']}`" if graph.get("track") else "")
    out = [f"### Impact matrix — live infra (`{graph['region']}`, {scope}) vs. code",
           "", f"Code declares: Spring Boot `{dec['spring_boot']}`, Java `{dec['java_version']}`, `{dec['persistence_api']}`, "
           f"health `:{dec['management_port']}{dec['health_path']}`, secret key `{dec['db_password_key']}`, ddl-auto `{dec['ddl_auto']}`.",
           "", "| # | component | infra expects | code declares | status | impact if deployed as-is |", "|---|---|---|---|---|---|"]
    for r in rows:
        out.append(f"| {r['gap'] or ''} | {r['component']} | {r['infra']} | {r['code']} | **{r['status']}** | {r['impact']} |")
    drift = [r for r in rows if r["status"] == "DRIFT"]
    out += ["", f"**{len(drift)} drift item(s), {len(rows) - len(drift)} OK.**"]
    return "\n".join(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--graph", default="demo/dependency-graph.json")
    ap.add_argument("--repo", default=".")
    ap.add_argument("--out")
    ap.add_argument("--no-fail", action="store_true")
    args = ap.parse_args()
    graph = json.loads(read(args.graph))
    dec = declared(args.repo)
    rows = matrix(dec, observed(graph))
    md = render(rows, dec, graph)
    print(md)
    if args.out:
        with open(args.out, "w") as f:
            f.write(md + "\n")
    drift = any(r["status"] == "DRIFT" for r in rows)
    sys.exit(1 if drift and not args.no_fail else 0)


if __name__ == "__main__":
    main()
