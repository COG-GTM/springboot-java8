#!/usr/bin/env python3
"""REST parity harness for springboot-java8.

  capture  Start a built jar once per scenario, replay scenarios.json, write a snapshot.
  compare  Diff two snapshots, check endpoint coverage, write a Markdown report.

Only the Python 3 standard library is used, so the same harness runs unchanged
against the Java 8 / Spring Boot 2 build and the Java 21 / Spring Boot 3 build.
"""
import argparse
import datetime
import fnmatch
import http.client
import json
import os
import re
import shutil
import socket
import subprocess
import sys
import tempfile
import time
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
COMPARED_HEADERS = ["content-type", "allow"]
# Printed by Application.main() after SpringApplication.run() returns, i.e. after all
# CommandLineRunners finished, so startup output is complete before requests start.
READY_MARKER = "Let's inspect the beans provided by Spring Boot"
JVM_OPTS = ["-Duser.timezone=UTC", "-Duser.language=en", "-Duser.country=US", "-Dfile.encoding=UTF-8"]

# Normalisation applied identically to both sides. Values that legitimately
# change between runs are masked; their *shape* is kept so format drift
# (e.g. extra fractional-second digits) still shows up as a difference.
ISO_DATETIME = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.(\d+))?)?")
ERROR_TIMESTAMP = re.compile(r'"timestamp":"[^"]*"')


def mask_datetime(m):
    frac = m.group(1)
    shape = "frac=%d" % len(frac) if frac else "no-frac"
    return "<DATETIME %s>" % shape


def normalize(body):
    body = ERROR_TIMESTAMP.sub('"timestamp":"<TIMESTAMP>"', body)
    return ISO_DATETIME.sub(mask_datetime, body)


# --------------------------------------------------------------------------- endpoints
MAPPING = re.compile(r"@(Request|Get|Post|Put|Delete|Patch)Mapping\s*(\(([^)]*)\))?\s*\n?\s*public")


def scan_endpoints(src_dir):
    """Statically list controller mappings as (METHOD|ANY, path, file)."""
    endpoints = []
    for root, _, files in os.walk(src_dir):
        for f in sorted(files):
            if not f.endswith(".java"):
                continue
            text = open(os.path.join(root, f), encoding="utf-8").read()
            if "@RestController" not in text and "@Controller" not in text:
                continue
            for m in MAPPING.finditer(text):
                kind, args = m.group(1), m.group(3) or ""
                paths = re.findall(r'"([^"]*)"', args) or [""]
                if kind == "Request":
                    meth = re.search(r"RequestMethod\.(\w+)", args)
                    method = meth.group(1) if meth else "ANY"
                else:
                    method = kind.upper()
                for p in paths:
                    endpoints.append({"method": method, "path": p, "file": f})
    return sorted(endpoints, key=lambda e: (e["path"], e["method"]))


def endpoint_regex(path):
    return re.compile("^" + re.sub(r"\{[^}]+\}", "[^/]+", re.escape(path).replace(r"\{", "{").replace(r"\}", "}")) + "$")


def covering_steps(endpoint, scenarios):
    rx = endpoint_regex(endpoint["path"])
    hits = []
    for sc in scenarios:
        for st in sc["steps"]:
            path = st["request"]["path"].split("?")[0]
            method = st["request"]["method"]
            if not rx.match(path):
                continue
            if endpoint["method"] == "ANY" or endpoint["method"] == method:
                hits.append("%s/%s" % (sc["name"], st["id"]))
    return hits


# --------------------------------------------------------------------------- capture
def free_port():
    s = socket.socket()
    s.bind(("127.0.0.1", 0))
    port = s.getsockname()[1]
    s.close()
    return port


def build_workdir(spec):
    wd = tempfile.mkdtemp(prefix="parity-wd-")
    for rel in spec.get("empty_files", []):
        p = os.path.join(wd, rel)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        open(p, "w").close()
    for rel, src in spec.get("copy_files", {}).items():
        shutil.copyfile(os.path.join(HERE, src), os.path.join(wd, rel))
    return wd


def start_app(java, jar, workdir, port, log_path):
    log = open(log_path, "w")
    proc = subprocess.Popen([java] + JVM_OPTS + ["-jar", jar, "--server.port=%d" % port],
                            cwd=workdir, stdout=log, stderr=subprocess.STDOUT)
    deadline = time.time() + 120
    while time.time() < deadline:
        if proc.poll() is not None:
            raise SystemExit("app exited during startup, see %s" % log_path)
        if READY_MARKER in open(log_path, errors="replace").read():
            return proc
        time.sleep(0.25)
    proc.kill()
    raise SystemExit("app did not start within 120s, see %s" % log_path)


def send(port, step):
    headers = dict(step.get("headers", {}))
    body = None
    if "json" in step:
        body = json.dumps(step["json"]).encode()
        headers.setdefault("Content-Type", "application/json")
    elif "raw" in step:
        body = step["raw"].encode()
    conn = http.client.HTTPConnection("127.0.0.1", port, timeout=30)
    conn.request(step["method"], step["path"], body=body, headers=headers)
    resp = conn.getresponse()
    raw = resp.read().decode("utf-8", errors="replace")
    hdrs = {k.lower(): v for k, v in resp.getheaders()}
    conn.close()
    return {
        "status": resp.status,
        "headers": {h: hdrs.get(h) for h in COMPARED_HEADERS},
        "other_headers": sorted(k for k in hdrs if k not in COMPARED_HEADERS),
        "body": raw,
        "body_normalized": normalize(raw),
    }


def jar_manifest(jar):
    with zipfile.ZipFile(jar) as z:
        text = z.read("META-INF/MANIFEST.MF").decode()
    return dict(line.split(": ", 1) for line in text.splitlines() if ": " in line)


def java_version(java):
    out = subprocess.run([java, "-version"], capture_output=True, text=True).stderr
    return out.splitlines()[0] if out else "unknown"


def git_sha():
    try:
        return subprocess.run(["git", "rev-parse", "--short", "HEAD"], cwd=REPO,
                              capture_output=True, text=True).stdout.strip()
    except OSError:
        return "unknown"


def capture(args):
    spec = json.load(open(args.scenarios))
    java = os.path.join(args.java_home, "bin", "java") if args.java_home else "java"
    args.jar = os.path.abspath(args.jar)
    manifest = jar_manifest(args.jar)
    log_dir = tempfile.mkdtemp(prefix="parity-logs-")
    snapshot = {
        "meta": {
            "label": args.label,
            "java": java_version(java),
            "spring_boot": manifest.get("Spring-Boot-Version", "unknown"),
            "build_jdk": manifest.get("Build-Jdk-Spec", manifest.get("Build-Jdk", "unknown")),
            "git_sha": git_sha(),
            "captured_at": datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="seconds"),
            "jvm_opts": JVM_OPTS,
        },
        "endpoints": scan_endpoints(args.src),
        "scenarios": [],
    }
    for sc in spec["scenarios"]:
        wd = build_workdir(spec.get("workdir", {}))
        port = free_port()
        log_path = os.path.join(log_dir, sc["name"] + ".log")
        proc = start_app(java, args.jar, wd, port, log_path)
        steps = []
        try:
            for st in sc["steps"]:
                req = {k: st[k] for k in ("method", "path", "headers", "json", "raw") if k in st}
                steps.append({"id": st["id"], "request": req, "response": send(port, st)})
        finally:
            proc.terminate()
            try:
                proc.wait(20)
            except subprocess.TimeoutExpired:
                proc.kill()
        startup = [re.sub(r"^.*hello\.Application\s+:\s*", "", l.rstrip())
                   for l in open(log_path, errors="replace") if "Customer{" in l]
        snapshot["scenarios"].append({"name": sc["name"], "startup_customers": startup, "steps": steps})
        shutil.rmtree(wd, ignore_errors=True)
        print("captured %-16s %d steps" % (sc["name"], len(steps)))
    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "w") as f:
        json.dump(snapshot, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("wrote %s (logs in %s)" % (args.out, log_dir))


# --------------------------------------------------------------------------- compare
def step_diffs(b, c):
    """Return [(field, baseline_value, candidate_value)] for one request."""
    diffs = []
    if b["status"] != c["status"]:
        diffs.append(("status", b["status"], c["status"]))
    for h in COMPARED_HEADERS:
        if b["headers"].get(h) != c["headers"].get(h):
            diffs.append((h, b["headers"].get(h), c["headers"].get(h)))
    if b["body_normalized"] != c["body_normalized"]:
        diffs.append(("body", b["body_normalized"], c["body_normalized"]))
    return diffs


def only_error_message_differs(bb, cb):
    try:
        bj, cj = json.loads(bb), json.loads(cb)
    except ValueError:
        return False
    if not (isinstance(bj, dict) and isinstance(cj, dict) and "message" in bj and "message" in cj):
        return False
    strip = lambda d: {k: v for k, v in d.items() if k != "message"}
    return list(bj) == list(cj) and strip(bj) == strip(cj)


def accepting_rule(key, diff, rules):
    field, bv, cv = diff
    for r in rules:
        if not fnmatch.fnmatch(key, r["steps"]):
            continue
        if r["field"] == "error-message":
            if field == "body" and only_error_message_differs(bv, cv):
                return r
        elif r["field"] == field and r.get("baseline", bv) == bv and r.get("candidate", cv) == cv:
            return r
    return None


def describe(diff):
    field, bv, cv = diff
    return "body" if field == "body" else "%s %r -> %r" % (field, bv, cv)


def md_escape(s):
    return s.replace("|", "\\|").replace("\n", " ")


def diff_block(w, bv, cv):
    w("")
    w("```diff")
    w("- " + bv)
    w("+ " + cv)
    w("```")


def compare(args):
    base = json.load(open(args.baseline))
    cand = json.load(open(args.candidate))
    rules = json.load(open(args.accepted))["rules"] if args.accepted else []
    rows, failures, accepted = [], [], []
    total = identical = 0
    cand_by_name = {s["name"]: s for s in cand["scenarios"]}
    for bs in base["scenarios"]:
        cs = cand_by_name.get(bs["name"])
        if cs is None:
            failures.append(("%s/*" % bs["name"], [("scenario", "present", "missing")]))
            continue
        if bs["startup_customers"] != cs["startup_customers"]:
            failures.append(("%s/startup" % bs["name"], [("startup JDBC output", "\n".join(bs["startup_customers"]),
                                                          "\n".join(cs["startup_customers"]))]))
        cand_steps = {s["id"]: s for s in cs["steps"]}
        for st in bs["steps"]:
            total += 1
            key = "%s/%s" % (bs["name"], st["id"])
            ct = cand_steps.get(st["id"])
            b = st["response"]
            if ct is None:
                failures.append((key, [("step", "present", "missing")]))
                rows.append((bs["name"], st, b, None, "**MISSING**"))
                continue
            diffs = step_diffs(b, ct["response"])
            unexplained = [d for d in diffs if not accepting_rule(key, d, rules)]
            if not diffs:
                identical += 1
                verdict = "identical"
            elif unexplained:
                failures.append((key, unexplained))
                verdict = "**DIFFERENT**"
            else:
                accepted.append((key, [(d, accepting_rule(key, d, rules)) for d in diffs]))
                verdict = "accepted: " + ", ".join(sorted({accepting_rule(key, d, rules)["id"] for d in diffs}))
            rows.append((bs["name"], st, b, ct["response"], verdict))

    endpoint_rows = []
    uncovered = []
    for ep in base["endpoints"]:
        hits = covering_steps(ep, base["scenarios"])
        endpoint_rows.append((ep, hits))
        if not hits:
            uncovered.append(ep)
    ep_key = lambda e: (e["method"], e["path"])
    inventory_same = sorted(map(ep_key, base["endpoints"])) == sorted(map(ep_key, cand["endpoints"]))

    ok = not failures and not uncovered and inventory_same
    out = []
    w = out.append
    w("# REST parity report: %s vs %s" % (base["meta"]["label"], cand["meta"]["label"]))
    w("")
    w("**Result: %s**" % ("PASS" if ok else "FAIL"))
    w("")
    w("- %d requests replayed in %d scenarios: %d identical, %d accepted differences, %d unexplained differences."
      % (total, len(base["scenarios"]), identical, len(accepted), total - identical - len(accepted)))
    w("- %d/%d endpoints covered; endpoint inventory %s between the two builds."
      % (len(endpoint_rows) - len(uncovered), len(endpoint_rows), "identical" if inventory_same else "DIFFERS"))
    w("- Startup JDBC output (customers query) %s in every scenario."
      % ("identical" if not any(k.endswith("/startup") for k, _ in failures) else "DIFFERS"))
    w("")
    w("| | Baseline | Candidate |")
    w("|---|---|---|")
    for k in ("java", "spring_boot", "build_jdk", "git_sha", "captured_at"):
        w("| %s | `%s` | `%s` |" % (k, base["meta"].get(k), cand["meta"].get(k)))
    w("")
    w("Compared per request: HTTP status, `Content-Type`, `Allow`, and the full response body. "
      "Normalisation (both sides): error `timestamp` values are masked; ISO date-times are masked "
      "to `<DATETIME frac=N>` so the value is ignored but the format (fractional-second digits) "
      "is still compared. Each scenario runs in a fresh JVM with a fresh working directory "
      "(`scenarios.json` > `workdir`) and JVM options `%s`." % " ".join(base["meta"]["jvm_opts"]))
    w("")
    w("## Endpoint coverage")
    w("")
    w("Endpoints discovered statically from the controllers (`@RequestMapping` etc.); `ANY` means the "
      "mapping does not restrict the HTTP method.")
    w("")
    w("| Method | Path | Controller | Requests covering it |")
    w("|---|---|---|---|")
    for ep, hits in endpoint_rows:
        w("| %s | `%s` | %s | %d |" % (ep["method"], ep["path"], ep["file"], len(hits)))
    w("")
    if failures:
        w("## Unexplained differences")
        w("")
        for key, diffs in failures:
            w("### `%s`" % key)
            w("")
            for d in diffs:
                w("- %s" % describe(d))
                if d[0] in ("body", "startup JDBC output"):
                    diff_block(w, str(d[1]), str(d[2]))
            w("")
    if accepted:
        w("## Accepted differences")
        w("")
        for r in rules:
            hits = [(k, d) for k, ds in accepted for d, rr in ds if rr is r]
            if not hits:
                continue
            w("### `%s` (%d requests)" % (r["id"], len(hits)))
            w("")
            w(r["reason"])
            w("")
            for k, d in hits:
                w("- `%s`: %s" % (k, describe(d)))
                if d[0] == "body":
                    diff_block(w, d[1], d[2])
            w("")
    w("## All requests")
    w("")
    w("| Scenario | Step | Request | Baseline | Candidate | Result |")
    w("|---|---|---|---|---|---|")
    for scen, st, b, c, verdict in rows:
        req = "%s %s" % (st["request"]["method"], st["request"]["path"])
        fmt = lambda r: "-" if r is None else "%s `%s`" % (r["status"], (r["headers"].get("content-type") or "no body"))
        w("| %s | %s | `%s` | %s | %s | %s |" % (scen, st["id"], md_escape(req), fmt(b), fmt(c), verdict))
    w("")
    report = "\n".join(out)
    if args.report:
        with open(args.report, "w") as f:
            f.write(report)
        print("wrote %s" % args.report)
    print("%s: %d requests, %d identical, %d accepted, %d unexplained; %d uncovered endpoints; inventory %s"
          % ("PASS" if ok else "FAIL", total, identical, len(accepted), len(failures), len(uncovered),
             "same" if inventory_same else "differs"))
    for key, diffs in failures:
        print("  DIFF %s: %s" % (key, "; ".join(describe(d) for d in diffs)))
    return 0 if ok else 1


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    c = sub.add_parser("capture")
    c.add_argument("--jar", required=True)
    c.add_argument("--java-home", help="JDK to run the jar with (default: java on PATH)")
    c.add_argument("--label", required=True)
    c.add_argument("--out", required=True)
    c.add_argument("--scenarios", default=os.path.join(HERE, "scenarios.json"))
    c.add_argument("--src", default=os.path.join(REPO, "src", "main", "java"))
    k = sub.add_parser("compare")
    k.add_argument("baseline")
    k.add_argument("candidate")
    k.add_argument("--report")
    k.add_argument("--accepted", default=os.path.join(HERE, "accepted-differences.json"),
                   help="reviewed differences that do not fail the comparison ('' to disable)")
    a = ap.parse_args()
    if a.cmd == "capture":
        capture(a)
    else:
        sys.exit(compare(a))


if __name__ == "__main__":
    main()
