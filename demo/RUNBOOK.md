# Runbook — Java 8/11 + Spring Boot 2.7 → Java 21 + Spring Boot 3 (15 minutes)

**Story.** The Claude Code plugin has already migrated the code: `plugin/migration` compiles on Java 21, unit tests pass,
PR is open. Devin picks up *after* that and runs the infra-aware phases the laptop tool cannot:
**Assess → Plan → Execute/Verify → Complete**. Six gaps are planted (see `PLANTED-GAPS.md`); each act maps to them.

| gap | what a repo-only tool cannot see |
|---|---|
| 1 | Infrastructure discovery — LB, DNS, firewall, secrets |
| 2 | Database dependencies |
| 3 | Cross-component / cross-repo impact |
| 4 | NFR validation post-deployment vs. a baseline |
| 5 | Observability baselining before vs. after |
| 6 | Monitoring pipelines and iterating autonomously to green |

## Before the demo (T-30 min)

```bash
make aws-up            # real footprint: ALB, SGs, Route53 private zone, Secrets Manager, RDS Postgres, EC2 t3.small + ECR  (~10 min)
make aws-discover      # refreshes demo/dependency-graph.{json,md,png} from live AWS APIs
make reset && make up  # local compose stack on the legacy branch; Grafana at http://localhost:3000/d/topics-api
```

Approximate footprint cost: **≈ $0.08 / hour** (ALB $0.0225 + LCU, t3.small $0.0208, db.t4g.micro $0.016 + 20 GB gp3 ≈ $0.003,
Route53 private zone $0.50/month, Secrets Manager $0.40/month). Destroy with `make aws-down` immediately after — it prints
`clean: 0 tagged resources remain`.

Have open: the plugin PR (`plugin/migration`), Devin, Grafana, `demo/dependency-graph.png`.

---

## Act 1 — Assess (4 min) · *Ask Devin*

### Beat 1a — the "Wiz-style" discovery
**Prompt (paste into Ask Devin):**
> Before we deploy the plugin/migration PR, discover the live footprint of the topics-api in AWS (Project=modernization-demo) using `make aws-discover` and show me the dependency graph. Include the LB, health check, security groups, DNS, secret and database it depends on.

**Expected:** Devin runs the agentless discovery (ELBv2, TGs, EC2, SGs, Route53, Secrets Manager, RDS, ECR — read-only, by tag),
attaches `dependency-graph.png` and lists the contracts: ALB `:80 → tg :8080`, health check `GET :8080/manage/health/readiness`,
app SG admits **only 8080** from the ALB SG, `db.topics.demo.internal → RDS postgres 16`, secret `moddemo-topics/db` with key **`DB_PASSWORD`**,
EC2 runs image built `FROM eclipse-temurin:11-jre`.

**Money moment:** the rendered graph with the edge labels `health_checks path=/manage/health/readiness`, `allows_ingress port=8080`,
`reads_secret key=DB_PASSWORD`, `runs_image base=eclipse-temurin:11-jre`.

**Presenter note — Wiz contrast:** Wiz maps the *security* graph to find risk. Devin maps the *application dependency graph for change*:
the same ALB/SG/DNS/secret nodes, but the question is "what breaks when this code changes", not "what is exposed".

### Beat 1b — impact matrix
**Prompt:**
> Now check out plugin/migration and diff what that code expects against the graph you just discovered — health path and port, admitted ports, secret key names, base image, DB driver/dialect and schema contract. Also list every consumer of the topics database, including anything outside this repo.

**Expected:** Devin runs `tools/discover/diff.py` and reports **6 DRIFT rows** (see `PLANTED-GAPS.md` §Impact matrix): JDK 21 class files on an
11 runtime, health check moved to `:8081/actuator`, secret key renamed to `TOPICS_DB_PASSWORD`, `ddl-auto=update` + `@Column(name="description")`
on a schema shared with the reporting cron, SG missing 8081, pool/logging defaults changed. It names the second DB consumer
(`infra/reporting/report.sh` reading `topic_summary_v` / `topic_report()`).

**Money moment:** the matrix — one row per contract, `OK`/`DRIFT`, "impact if deployed as-is".

**Takeaway:** *Claude Code sees the repo; Devin sees the repo **and** the ALB, SG, DNS record and secret it depends on.* (gaps 1, 2, 3)

---

## Act 2 — Plan (3 min) · *Ask Devin*

**Prompt:**
> Open an infra PR on top of plugin/migration that fixes only the drift in your matrix and link it to the plugin PR. Post the plan in the description: ordered fixes, rollback, and the NFR gates you will enforce against demo/BASELINE.json.

**Expected:** a PR touching `Dockerfile`, `infra/terraform`, `infra/aws`, `infra/nginx`, `infra/firewall`, `infra/observability/prometheus.yml`,
`infra/secrets`, `infra/docker-compose.yml` and the DB contract — no Java changes. Description = impact matrix → fix list → gates
(p95 ≤ max(1.5×, +25 ms), errors ≤ 1 %, throughput ≥ 60 %). Ends with `Devin-Org: engineering`.

**Money moment:** PR description side-by-side with the plugin PR: "code PR" + "infra PR" as a linked pair.

**Takeaway:** *The migration is two PRs, not one — and the second one needs infrastructure the plugin never saw.* (gap 3)

---

## Act 3 — Execute → Verify (6 min) · *Devin session with playbook*

**Prompt (kicks off the full session):**
> Run Modernization Verify [v1] @playbook:playbook-<id> on the infra PR against infra/ and the AWS footprint, iterating until CI and `make verify` are green. Compare k6 against demo/BASELINE.json, explain any regression from Grafana, and attach the Verify report.

**Expected sequence (this is the cascade in `PLANTED-GAPS.md`):**
1. CI runs on the infra PR; Devin watches the jobs, reads logs, pushes fixes until green.
2. `make reset && make up` on the migrated image → each planted layer surfaces in order and is fixed:
   `UnsupportedClassVersionError` (gap 1) → password missing (gap 3) → nginx `/health` 404 (gap 2) → 502 from firewall on 8081 +
   Prometheus target down (gap 5).
3. Smoke: **GET /topic returns 500 — `column t1_0.description does not exist`** and Hibernate's `ddl-auto=update` is refused by Postgres
   (`cannot alter type of a column used by a view or rule … topic_summary_v`) — the second DB consumer is what blocked the mutation (gap 4).
4. `make loadtest` + `make compare` against the legacy baseline: **p95 ≈ 20× and throughput ≈ ⅓** (gap 6). Devin opens Grafana: the
   *Hikari active/pending* panel is empty — the plugin swapped Hikari for `DriverManagerDataSource` (a new TCP+SCRAM connection per request) and
   turned SQL logging on. Fixes: restore column mapping + `ddl-auto=validate`; restore Hikari (pool=20, `open-in-view=false`, SQL logging off).
   Re-run → `verify OK`, `compare` within tolerance.
5. Browser walkthrough of the LB endpoints and the Grafana dashboard, screen-recorded.

**Money moments:** (a) the `compare_nfr.py` FAIL block — legacy p95 vs migrated p95 side by side; (b) the Grafana *Hikari active/pending* panel
going blank while *log events/s* spikes at the load-test start; (c) CI going red → green without a human touching it.

**Takeaway:** *A migration that passes unit tests can still fail its baseline — you only find out by deploying, loading, and reading the dashboards.* (gaps 4, 5, 6)

---

## Act 4 — Complete (2 min)

No new prompt — the playbook's final phase produces it. Show the filled `VERIFY-REPORT-TEMPLATE.md` posted on both PRs:
inventory from discovery, impact matrix with before/after, NFR table (baseline / first run / after fixes), observability deltas,
residual risks.

**Money moment:** the NFR table's three columns, and "Cloud footprint destroyed: 0 tagged resources remain".

**Takeaway:** *Complete means verified against running infrastructure and a baseline, with evidence — not "it compiled".* (gaps 4, 6)

---

## Sidebar — How this integrates with Wiz

If the customer runs Wiz, Devin can ingest the Wiz Security Graph as an additional Assess input: the same ALB, security groups,
secrets and databases appear as Wiz graph entities with risk context (exposure, misconfiguration, known vulnerabilities on the
base image). Devin would query it through the Wiz GraphQL API with a service-account token and merge the results into the
dependency graph — e.g. flagging that `eclipse-temurin:11-jre` carries CVEs Wiz already tracks, so the runtime pin fix is also a
risk fix. No implementation is included here; a sample query:

```graphql
query AppDependencies($project: String!) {
  graphSearch(
    query: {
      type: ["LOAD_BALANCER", "SECURITY_GROUP", "DNS_ZONE", "SECRET", "DATABASE", "VIRTUAL_MACHINE", "CONTAINER_IMAGE"]
      where: { tags: { EQUALS: [{ key: "Project", value: $project }] } }
      relationships: [
        { type: [{ type: ROUTES_TO }, { type: PROTECTS }, { type: USES }, { type: CONNECTED_TO }], with: { type: ["VIRTUAL_MACHINE", "DATABASE", "SECRET"] } }
      ]
    }
    first: 100
  ) {
    nodes { entities { id name type properties } }
  }
}
```

## Timing

| act | minutes |
|---|---|
| Assess | 4 |
| Plan | 3 |
| Execute → Verify | 6 |
| Complete | 2 |

## After the demo

`make aws-down` (confirm `clean: 0 tagged resources remain`), then `demo/RESET.md`.
