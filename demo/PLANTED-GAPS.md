# Planted gaps — `plugin/migration`

This branch is what a code-only transform plugin produces for Java 11/Boot 2.7 → **Java 21/Boot 3.3.5**: `javax`→`jakarta`, new
property names, Boot 3 actuator defaults, a "modernised" datasource. **It compiles and all 9 unit tests pass** (`./mvnw test`), yet
`make verify` fails at the first layer and keeps failing as each layer is peeled back. Nothing outside `pom.xml`, `build.gradle`,
`Topic.java` and `application.properties` was touched — that is the point. **Do not fix these on this branch**; Devin fixes them in the
Execute/Verify act (on the infra PR, not here). Reset: `demo/RESET.md`.

| # | gap (maps to the six capability gaps) | where the plugin changed code | what still assumes the legacy contract |
|---|---|---|---|
| 1 | Runtime pin drifts (infra discovery) | `pom.xml` `java.version=21`, Boot 3.3.5 → JDK 21 class files (61.0) | `Dockerfile` `ARG BASE_IMAGE=eclipse-temurin:11-jre`, `infra/terraform/variables.tf` `runtime_image`, `infra/aws/variables.tf` `runtime_image`, `infra/.env` |
| 2 | Health path/port mismatch (infra discovery) | `management.server.port=8081`, base path `/actuator` (Boot 3 default; legacy was `/manage` on :8080) | `infra/nginx/conf.d/topics.conf` `/health → :8080/manage/health/readiness`, `docker-compose.yml` healthcheck, AWS target group `/manage/health/readiness`, Prometheus scrape `/manage/prometheus` |
| 3 | Secret key renamed (secrets) | `spring.datasource.password=${TOPICS_DB_PASSWORD}` (`TOPICS_DB_URL`, `TOPICS_DB_USER`) | `infra/secrets/.env.example` + `app.env` (`DB_PASSWORD`), compose `DB_URL`/`DB_USER`, Secrets Manager JSON key `DB_PASSWORD`, `user-data.sh.tftpl` `.DB_PASSWORD`, `reporting/report.sh` |
| 4 | DB dependency breaks (database) | `@Column(name = "description")` on `subjectDescription`; `ddl-auto=update` (was `validate`) | `infra/db/init/01-schema.sql` column `subject_description NOT NULL`; view `topic_summary_v` + `topic_report()` and the reporting cron read `subject_description` and pin `topics.id`'s type |
| 5 | Firewall/DNS missing for the new port (infra discovery) | actuator moved to **:8081** | `infra/firewall/rules.conf` allows only `tcp 8080`; nginx `allowlist` + `deny /manage/`; no route for `/actuator`; `infra/dns/hosts`/Route53 have no metrics name; AWS app SG allows ALB→:8080 only |
| 6 | Load-only NFR regression (NFR + observability) | `spring.datasource.type=DriverManagerDataSource` (no pool), Hikari `maximum-pool-size=20/minimum-idle=5` dropped, `open-in-view` back to default `true`, `spring.jpa.show-sql=true` + bind TRACE | `demo/BASELINE.json` (Hikari pool 20, p95 1.7 ms, 2139 rps); Grafana *Hikari active/pending* panel; `compare_nfr.py` tolerances |

## Impact matrix (Assess act artifact)

Produced by `python tools/discover/diff.py --graph demo/dependency-graph.json --repo . --no-fail` on this branch against the live
`Track=java` AWS footprint captured in `demo/dependency-graph.json`:

### Impact matrix — live infra (`us-east-1`, `Project=modernization-demo`, `Track=java`) vs. code

Code declares: Spring Boot `3.3.5`, Java `21`, `jakarta.persistence`, health `:8081/actuator/health/readiness`, secret key `TOPICS_DB_PASSWORD`, ddl-auto `update`.

| # | component | infra expects | code declares | status | impact if deployed as-is |
|---|---|---|---|---|---|
| 1 | ec2:moddemo-topics-app base image | eclipse-temurin:11-jre (JDK 11) | java.version=21, Dockerfile ARG BASE_IMAGE=eclipse-temurin:11-jre, Boot 3.3.5 | **DRIFT** | container exits with UnsupportedClassVersionError; target never healthy |
| 2 | tg:moddemo-topics-tg health check | GET :8080/manage/health/readiness -> 200 | GET :8081/actuator/health/readiness | **DRIFT** | ALB marks target unhealthy (404/timeout); all traffic 503 |
| 3 | secret:moddemo-topics/db key | keys=['DB_PASSWORD', 'DB_USER']; host reads .DB_PASSWORD | spring.datasource.password=${TOPICS_DB_PASSWORD} | **DRIFT** | password resolves empty; Postgres rejects auth; app fails to start |
| 4 | rds:moddemo-topics-db schema contract | postgres 16.13 owned by infra/db/init (ddl-auto=validate expected) | ddl-auto=update, jakarta.persistence, driver=managed-by-boot, @Column(name=) overrides=['description'] | **DRIFT** | app mutates a schema shared with the reporting consumer (view/function break or NOT NULL violations) |
| 5 | app SG ingress :8080 | ALB -> app allowed on [8080] | app listens on [8080, 8081] | **OK** | health/metrics port silently dropped by the security group |
| 5 | app SG ingress :8081 | ALB -> app allowed on [8080] | app listens on [8080, 8081] | **DRIFT** | health/metrics port silently dropped by the security group |
| 5 | DB DNS name | records=['app.topics.demo.internal', 'db.topics.demo.internal', 'lb.topics.demo.internal'] | default DB host db.topics.internal (overridable by DB_URL) | **OK** | name does not resolve inside the VPC |
| 6 | connection pool / logging | baseline: Hikari pool=20, open-in-view=false, no SQL logging | datasource=DriverManagerDataSource, pool=none (connection per request), open-in-view=true (framework default), show-sql=true | **DRIFT** | latency/throughput regression under load only -> compare k6 vs demo/BASELINE.json |

**6 drift item(s), 2 OK.** (Same command on the legacy branch: `0 drift item(s), 7 OK.`)

## Observed failure cascade (`make verify` on this branch, JDK 21 build)

Each step below was reproduced by temporarily unblocking only the previous layer (image override, then a `TOPICS_DB_PASSWORD` line in
`infra/secrets/app.env`, then the column mapping) so the next one surfaces. None of those workarounds are committed.

**Layer 1 — `make up` fails, `make verify` never reaches the LB (gap 1):**
```
waiting for load balancer http://localhost:8088/health ...
load balancer never became healthy
app-1  | Error: LinkageError occurred while loading main class org.springframework.boot.loader.launch.JarLauncher
app-1  | 	java.lang.UnsupportedClassVersionError: org/springframework/boot/loader/launch/JarLauncher has been compiled by a more recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only recognizes class file versions up to 55.0
make: *** [Makefile:31: up] Error 1
```
`make verify` alone (stack down): `curl: (7) Failed to connect to localhost port 8088 … make: *** [Makefile:47: health] Error 7`.

**Layer 2 — with `RUNTIME_IMAGE=eclipse-temurin:21-jre` (gap 3):**
```
app-1  | ERROR … o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: password authentication failed for user "topics"
app-1  | Caused by: org.hibernate.service.spi.ServiceException: Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] due to: Unable to determine Dialect without JDBC metadata
load balancer never became healthy
```

**Layer 3 — with the secret also provided under the new key (gaps 2, 5, 4):**
```
$ curl -s -o /dev/null -w '%{http_code}' localhost:8088/health            -> 404   # nginx proxies to :8080/manage/…, actuator is on :8081/actuator
nginx-1 | upstream: "http://172.28.0.20:8080/manage/health/readiness" … 404
$ (from prometheus container) wget http://app.topics.internal:8081/actuator/health/readiness
wget: download timed out                                                   # firewall sidecar allows only tcp 8080
$ curl -s localhost:8088/topic
{"status":500,"error":"Internal Server Error","path":"/topic"}
app-1  | Hibernate: alter table if exists topics alter column id set data type varchar(255)
app-1  | org.postgresql.util.PSQLException: ERROR: cannot alter type of a column used by a view or rule
app-1  |   Detail: rule _RETURN on view topic_summary_v depends on column "id"
app-1  | ERROR: column t1_0.description does not exist
app-1  |   … JDBC exception executing SQL [select t1_0.id,t1_0.description,t1_0.subject_name from topics t1_0]
```
`make verify` stops at `== nginx health check == … HTTP 404 … Error 22`; Prometheus target `topics-api` is `down`
(scrape `:8080/manage/prometheus` → 404).

**Layer 4 — with the column mapping also restored, `make loadtest && make compare` (gap 6):**
```
NFR comparison FAILED against baseline
metric             baseline   candidate  verdict
p50 ms                 0.82       16.52
p95 ms                  1.7        32.5  REGRESSION
p99 ms                 2.45       48.24
rps                 2138.88      744.77  REGRESSION
error rate                0           0  ok
report p95 ms           1.5       21.38
k6 threshold http_req_duration: p(95)<250: pass
k6 threshold http_req_failed: rate<0.01: pass
make: *** [Makefile:84: compare] Error 1
```
Grafana: *Hikari active/pending* and *Hikari max* panels go blank (no `hikaricp_*` series without Hikari); *log events/s* spikes with
`show-sql` + bind TRACE. The k6 thresholds still pass — only the baseline comparison catches it.

## Expected Devin findings (per gap)

1. Image/Terraform pin JDK 11 while the artifact is JDK 21 → bump `runtime_image`/`BASE_IMAGE` to `eclipse-temurin:21-jre` in compose, `infra/terraform`, `infra/aws`.
2. Health contract moved to `:8081/actuator/health/readiness` → update nginx `/health`, compose healthcheck, AWS target group, Prometheus scrape path/port (or pin `management.endpoints.web.base-path=/manage`, port 8080, and record that as a decision).
3. `TOPICS_DB_*` keys → update `.env.example`/`app.env`, compose env, Secrets Manager key + `user-data` reader (or map old→new in the deployment, never both).
4. `ddl-auto=update` + renamed column vs. view/function/reporting cron → restore `validate` and the `subject_description` mapping; flag the schema as shared with a second consumer.
5. `:8081` unreachable → firewall `rules.conf`, nginx allowlist/route, app SG rule, Prometheus target, DNS name for metrics.
6. `DriverManagerDataSource` / pool + logging defaults → restore Hikari (pool 20, `open-in-view=false`, SQL logging off); prove with `make compare` back within tolerance and the Hikari panels repopulated.
