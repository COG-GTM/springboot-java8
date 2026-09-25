-- Schema owned by infra/db. The app runs with ddl-auto=validate and never mutates the schema.
CREATE TABLE topics (
    id                  VARCHAR(64)   PRIMARY KEY,
    subject_name        VARCHAR(255)  NOT NULL,
    subject_description VARCHAR(1024) NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);
