# Reset between demo runs

## Local (docker-compose) — ~1 minute

```bash
git checkout <main-fixture-branch>          # legacy code
make reset                                  # compose down -v: drops Postgres data (the migrated branch ALTERs the schema), Grafana state, load/results
make up && make smoke                       # legacy green
make baseline                               # optional: refresh demo/BASELINE.json (only if the host changed)
git checkout plugin/migration               # planted-gap branch, untouched
```

`make reset` is required (not just `make down`) after any run of the `plugin/migration` app: with
`ddl-auto=update` it adds a `description` column to `topics`, and that change lives in the `pgdata` volume.

Undo any fixes applied live during Act 3 with `git checkout -- infra/ Dockerfile src/main/resources` on the
migration branch, or reset the branch to the PR head: `git fetch && git reset --keep origin/plugin/migration`.

## AWS footprint — ~10 minutes

```bash
make aws-down            # terraform destroy + tag sweep; must print "clean: 0 tagged resources remain"
make aws-up              # ECR push + apply; RDS is the long pole (~6-8 min)
make aws-discover        # regenerates demo/dependency-graph.{json,md,png}
```

If `aws-down` reports leftover tagged resources, list them with
`aws resourcegroupstaggingapi get-resources --tag-filters Key=Project,Values=modernization-demo` and delete by hand;
Terraform state lives in `infra/aws/terraform.tfstate` (git-ignored) — do not delete it before destroying.

## Devin side

- Close the infra PR Devin opened in Act 2 (or leave it as an artefact and branch from a fresh copy).
- Re-run with the same prompts from `RUNBOOK.md`; the planted gaps are deterministic.
