ALTER TABLE audit_logs
    ADD COLUMN details_json JSON NULL;

UPDATE audit_logs
SET details_json = JSON_OBJECT('message', details)
WHERE details IS NOT NULL;

ALTER TABLE audit_logs
    DROP COLUMN details;

ALTER TABLE audit_logs
    CHANGE COLUMN details_json details JSON NULL;

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id, created_at);

CREATE INDEX idx_audit_logs_entity_type
    ON audit_logs (entity_type, created_at);

CREATE INDEX idx_audit_logs_user
    ON audit_logs (user_id, created_at);
