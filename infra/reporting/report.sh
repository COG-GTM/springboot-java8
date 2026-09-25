#!/bin/sh
# Second consumer of the topics DB: dumps topic_report() as JSON for downstream BI.
set -eu
export PGPASSWORD="${DB_PASSWORD:?DB_PASSWORD not set}"
OUT=${REPORT_OUT:-/reports/latest.json}
mkdir -p "$(dirname "$OUT")"
psql -qAt -v ON_ERROR_STOP=1 -c "
  SELECT json_build_object(
    'generated_at', now(),
    'total_topics', (SELECT count(*) FROM topic_summary_v),
    'topics',       (SELECT coalesce(json_agg(subject_name ORDER BY id), '[]'::json) FROM topic_summary_v),
    'report',       (SELECT coalesce(json_agg(r), '[]'::json) FROM topic_report() r)
  )" > "$OUT.tmp"
mv "$OUT.tmp" "$OUT"
cat "$OUT"
