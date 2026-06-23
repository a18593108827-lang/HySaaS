USE hysaas;

CREATE TABLE msg_email_log (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT,
    event_id    BIGINT,
    code        VARCHAR(64)  NOT NULL,
    recipient   VARCHAR(128) NOT NULL,
    subject     VARCHAR(256),
    status      VARCHAR(16)  NOT NULL,
    error_msg   VARCHAR(512),
    retry_count INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
