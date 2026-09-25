# secrets/

File-based stand-in for Vault / AWS Secrets Manager.

| Path | Consumer | Key |
|------|----------|-----|
| `vault-stub/db_password` | Postgres container (`POSTGRES_PASSWORD_FILE`) | n/a |
| `app.env` (from `.env.example`) | `app` (`spring.datasource.password=${DB_PASSWORD}`), `reporting` (`PGPASSWORD`) | `DB_PASSWORD` |

Rotating: change both files, `make reset`.
