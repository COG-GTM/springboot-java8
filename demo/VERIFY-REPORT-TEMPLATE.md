# Modernization Verify Report — `<app>` (`<legacy runtime>` → `<target runtime>`)

Produced by Devin at the **Complete** phase. Every row cites a command output, file or dashboard panel.

## 1. Summary

| | |
|---|---|
| Code PR (plugin) | `<url>` |
| Infra PR (Devin) | `<url>` |
| Verdict | GO / NO-GO |
| Blocking findings | `<n>` fixed, `<n>` open |
| Baseline | `demo/BASELINE.json` @ `<commit>` |

## 2. Inventory (discovered, not assumed)

Source: `demo/dependency-graph.json` (`make aws-discover`) and `infra/`.

| component | identifier | runtime contract observed |
|---|---|---|
| Load balancer | `<alb / nginx>` | listener :80 → target :`<port>` |
| Health check | `<tg / nginx location>` | `GET <path>` on :`<port>` expects 200 |
| Firewall | `<sg / rules.conf>` | admits `<ports>` from `<source>` |
| DNS | `<zone / Corefile>` | `<records>` |
| Secrets | `<secret arn / vault-stub>` | keys `<...>` |
| Database | `<rds / container>` | `<engine version>`; schema owner `infra/db/init`; consumers: app, `reporting` |
| Runtime image | `<ec2 tag / Dockerfile>` | `<base image>` |
| Observability | `<prometheus target / datadog tags>` | scrapes `<path>` on :`<port>` |

## 3. Impact matrix — app ↔ infra ↔ DB

Paste `tools/discover/diff.py` output, then add the resolution column.

| # | component | infra expected | code declared | status before | change made | status after |
|---|---|---|---|---|---|---|
| 1 | | | | DRIFT | | OK |
| … | | | | | | |

## 4. NFR before / after

Source: `demo/BASELINE.json` vs `load/results/latest.json` (`make loadtest`, same VUs/duration), `infra/scripts/compare_nfr.py`.

| metric | legacy baseline | migrated (first run) | migrated (after fixes) | tolerance | result |
|---|---|---|---|---|---|
| p50 ms | | | | | |
| p95 ms | | | | ≤ max(1.5×, +25 ms) | |
| p99 ms | | | | | |
| throughput req/s | | | | ≥ 60 % | |
| error rate | | | | ≤ 1 % | |
| k6 thresholds | pass | | | pass | |

## 5. Observability deltas

| signal | before | after | note |
|---|---|---|---|
| Prometheus target `up{job="topics-api"}` | 1 | | scrape path/port changed? |
| Grafana panel: p95 latency | | | screenshot links |
| Grafana panel: Hikari active/pending | | | pool size default |
| Grafana panel: log events/s | | | SQL logging |
| Datadog tags (`DD_ENV`, `version`) | | | version bump reflected? |

## 6. Cross-component checks

- Reporting consumer (`infra/reporting/report.sh`): `<latest.json still produced? view/function intact?>`
- Other consumers of the DB / secret / DNS name found by discovery: `<list or "none">`

## 7. Residual risks and follow-ups

| risk | severity | owner | mitigation |
|---|---|---|---|
| | | | |

## 8. Evidence

- CI run: `<url>`
- Screen recording of browser verification against the LB: `<attachment>`
- Grafana before/after screenshots: `<attachments>`
- Raw k6 summaries: `load/results/*.json`
