CREATE TABLE audit_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(60) NOT NULL,
    actor VARCHAR(128) NOT NULL,
    target VARCHAR(180) NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_event_created_at ON audit_event(created_at);
CREATE INDEX idx_audit_event_action ON audit_event(action);
