-- Consumed by BOTH the app (GET /topic/report) and the reporting cron (infra/reporting/report.sh).
CREATE VIEW topic_summary_v AS
SELECT id,
       subject_name,
       subject_description,
       length(id)  AS id_length,
       created_at
FROM topics;

CREATE FUNCTION topic_report()
RETURNS TABLE (bucket TEXT, topic_count BIGINT, newest_topic TEXT, sample_subject TEXT)
LANGUAGE sql STABLE AS $$
    SELECT CASE WHEN id_length > 5 THEN 'long' ELSE 'short' END AS bucket,
           count(*)                                              AS topic_count,
           (array_agg(id ORDER BY created_at DESC))[1]           AS newest_topic,
           min(subject_name)                                     AS sample_subject
    FROM topic_summary_v
    GROUP BY 1
    ORDER BY 1
$$;
