# Modernization Verify [v1]

## Overview
Infra-aware verification of a code-only runtime migration (e.g. Java 8/11 + Spring Boot 2.7 → Java 21 + Spring Boot 3).
The code transform PR already exists. This playbook discovers the running infrastructure, diffs it against what the
migrated code now expects, opens an infra PR, deploys, validates NFRs against the pre-migration baseline, and produces
the Verify report. Only ever create the TODO list for the current phase.

## What's Needed From User
- Repository and the migration PR/branch (e.g. `plugin/migration`)
- Environment to verify against: `infra/` (docker-compose) and/or the AWS footprint (`make aws-up`, tag `Project=modernization-demo`)
- Baseline file (`demo/BASELINE.json`) or permission to capture one from the legacy branch
- Where to post results (PR comment, Slack channel)

<phase name="Assess" id="1">
## Assess

1. Discover the live footprint, do not infer it from code: run `make aws-discover` (AWS) and read `infra/docker-compose.yml`,
   `infra/nginx`, `infra/firewall`, `infra/dns`, `infra/secrets`, `infra/terraform`, `Dockerfile` (local). Attach `demo/dependency-graph.png`.
2. Enumerate database dependencies: schema owner, views/functions, every consumer (app, `infra/reporting`, anything else in the graph).
3. Check out the migration branch and run `tools/discover/diff.py --repo . --graph demo/dependency-graph.json`; extend the
   impact matrix with anything the script cannot see (property renames, pool/logging defaults, actuator paths).
4. Search other repositories in the org for consumers of the same DB, DNS names or secret keys (cross-repo impact).

<verification>
- Dependency graph generated from live APIs/config, attached as PNG, not hand-drawn
- Impact matrix lists every app↔infra↔DB contract with status OK/DRIFT and expected failure mode
- Every DB view/function has its consumers listed
- Cross-repo search performed and results (or "none found") recorded
</verification>
</phase>

<phase name="Plan" id="2">
## Plan

1. Open an **infra PR** on a branch off the migration branch, linked to the plugin PR, containing only the drift fixes:
   runtime pin (Dockerfile ARG, `infra/terraform`, `infra/aws`), health-check path/port, firewall/DNS entries, secret key names,
   scrape config, DB contract (revert schema mutation or migrate the view/function with the reporting consumer).
2. Post the plan as the PR description: impact matrix, ordered fix list, rollback, and the NFR gates that will be enforced.
3. Do NOT change application code except configuration required to restore the infra contract.

<verification>
- Infra PR exists, references the plugin PR, description ends with the required org trailer
- Each DRIFT row in the matrix maps to a commit or an explicit "accepted, reason"
- NFR gates stated numerically (p95, error rate, throughput vs baseline)
</verification>
</phase>

<phase name="Execute and Verify" id="3">
## Execute and Verify

1. Run CI on the infra PR; iterate on failures until green — read the job logs, fix, push, re-check. Never disable a gate.
2. Deploy: `make reset && make up` (local) and/or `make aws-up` with the migrated image tag.
3. Baseline: if `demo/BASELINE.json` is missing, capture it from the legacy branch with `make baseline` first.
4. Load: `make loadtest` (same VUs/duration as baseline), then `make compare`. Investigate any regression in Grafana
   (latency, Hikari pending, log rate) and Prometheus before changing anything; state the root cause with evidence.
5. Fix, redeploy, re-run until `make verify` passes and `compare` is within tolerance.
6. Browser verification with screen recording: open the load balancer URL in the browser, exercise `GET /topic`,
   `GET /topic/report`, the health endpoint through the LB, and the Grafana dashboard; record the session and attach it.

<verification>
- CI green on the infra PR (link to run)
- `make verify` output attached and passing; `compare` within tolerance against demo/BASELINE.json
- Root cause of each regression stated with the metric/panel that showed it
- Screen recording of the browser walkthrough attached
</verification>
</phase>

<phase name="Complete" id="4">
## Complete

1. Fill `demo/VERIFY-REPORT-TEMPLATE.md` completely; every row cites a file, command output or panel.
2. Present the report in the session (attach the markdown) and post it as a comment on both PRs.
3. If deployed to AWS for verification, tear down with `make aws-down` and confirm zero tagged resources remain.

<verification>
- Report attached in-session and posted on the plugin PR and infra PR
- Residual risks table non-empty or explicitly "none"
- Cloud footprint destroyed (or handed over with an owner named)
</verification>
</phase>

## Specifications
- Baseline and migrated load runs use identical k6 parameters.
- Tolerances: p95 ≤ max(1.5× baseline, baseline + 25 ms); error rate ≤ 1 %; throughput ≥ 60 % of baseline.
- Every commit message contains "feature" or "bug"; every PR description ends with `Devin-Org: engineering`.

## Advice and Pointers
- Framework upgrades change defaults silently: actuator base path/port, `open-in-view`, Hikari pool size, property names.
- The reporting cron and the app share the schema; `ddl-auto=update` on one side is a cross-component change.
- Firewall drops are silent — a 502/timeout from the LB with a healthy container usually means a port is not admitted.

## Forbidden Actions
- Do not edit `demo/PLANTED-GAPS.md` or the plugin PR's Java sources to make verification pass.
- Do not loosen k6 thresholds or `compare_nfr.py` tolerances.
- Do not leave the AWS footprint running after Complete.
