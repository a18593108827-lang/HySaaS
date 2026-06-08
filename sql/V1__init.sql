-- 全量 DDL + 种子数据，docker 首次启动自动执行
CREATE DATABASE IF NOT EXISTS hysaas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hysaas;

CREATE TABLE sys_tenant (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    contact_email VARCHAR(128),
    address     VARCHAR(256),
    remark      VARCHAR(512),
    status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64)  NOT NULL,
    user_type   VARCHAR(16)  NOT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_username_deleted (username, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
    id          BIGINT NOT NULL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    tenant_id   BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
    id              BIGINT NOT NULL PRIMARY KEY,
    role_id         BIGINT NOT NULL,
    permission_id   BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_event_permission (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    event_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    perm_code   VARCHAR(64)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_config (
    id          BIGINT       NOT NULL PRIMARY KEY,
    config_key  VARCHAR(64)  NOT NULL,
    config_value TEXT,
    remark      VARCHAR(256),
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evt_event (
    id                   BIGINT       NOT NULL PRIMARY KEY,
    tenant_id            BIGINT       NOT NULL,
    title                VARCHAR(256) NOT NULL,
    location             VARCHAR(256),
    start_time           DATETIME,
    end_time             DATETIME,
    status               VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    registration_enabled TINYINT      NOT NULL DEFAULT 0,
    paper_enabled        TINYINT      NOT NULL DEFAULT 0,
    hotel_enabled        TINYINT      NOT NULL DEFAULT 0,
    invite_url           VARCHAR(512),
    qrcode_url           VARCHAR(512),
    checkin_token        VARCHAR(64),
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted              TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evt_registration (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    event_id    BIGINT       NOT NULL,
    user_id     BIGINT,
    name        VARCHAR(64)  NOT NULL,
    email       VARCHAR(128),
    phone       VARCHAR(32),
    member_type VARCHAR(32),
    status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    source      VARCHAR(16),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evt_checkin (
    id          BIGINT   NOT NULL PRIMARY KEY,
    tenant_id   BIGINT   NOT NULL,
    event_id    BIGINT   NOT NULL,
    user_id     BIGINT,
    checkin_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE paper_submission (
    id           BIGINT       NOT NULL PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    event_id     BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(512) NOT NULL,
    author       VARCHAR(256),
    status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    version      INT          NOT NULL DEFAULT 1,
    file_key     VARCHAR(256),
    submitted_at DATETIME,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE paper_review (
    id            BIGINT NOT NULL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    submission_id BIGINT NOT NULL,
    reviewer_id   BIGINT NOT NULL,
    comment       TEXT,
    suggest       VARCHAR(16),
    reviewed_at   DATETIME,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE paper_review_assignment (
    id            BIGINT NOT NULL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    submission_id BIGINT NOT NULL,
    reviewer_id   BIGINT NOT NULL,
    assigned_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_info (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    event_id    BIGINT       NOT NULL,
    name        VARCHAR(128) NOT NULL,
    address     VARCHAR(256),
    contact     VARCHAR(64),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_room_type (
    id          BIGINT        NOT NULL PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL,
    hotel_id    BIGINT        NOT NULL,
    name        VARCHAR(64)   NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT       NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_quota (
    id           BIGINT NOT NULL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL,
    event_id     BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    total        INT    NOT NULL DEFAULT 0,
    used         INT    NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_booking (
    id           BIGINT        NOT NULL PRIMARY KEY,
    tenant_id    BIGINT        NOT NULL,
    event_id     BIGINT        NOT NULL,
    user_id      BIGINT        NOT NULL,
    room_type_id BIGINT        NOT NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING_PAY',
    amount       DECIMAL(10,2),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pay_order (
    id           BIGINT        NOT NULL PRIMARY KEY,
    tenant_id    BIGINT        NOT NULL,
    user_id      BIGINT        NOT NULL,
    biz_type     VARCHAR(32)   NOT NULL,
    biz_id       BIGINT        NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pay_transaction (
    id              BIGINT       NOT NULL PRIMARY KEY,
    order_id        BIGINT       NOT NULL,
    trade_no        VARCHAR(64),
    alipay_trade_no VARCHAR(64),
    status          VARCHAR(16)  NOT NULL,
    notify_data     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inv_invoice (
    id          BIGINT        NOT NULL PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    order_id    BIGINT        NOT NULL,
    title       VARCHAR(128)  NOT NULL,
    tax_no      VARCHAR(32),
    amount      DECIMAL(10,2) NOT NULL,
    status      VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    file_url    VARCHAR(512),
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE msg_email_template (
    id          BIGINT       NOT NULL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    event_id    BIGINT,
    code        VARCHAR(64)  NOT NULL,
    subject     VARCHAR(256) NOT NULL,
    body        TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 测试账号：admin@test.com / ent@test.com / user@test.com，密码 123456
INSERT INTO sys_tenant (id, name, contact_name, contact_phone, contact_email, status) VALUES
(1, '演示企业', '张经理', '13800000001', 'ent@test.com', 'APPROVED');

INSERT INTO sys_user (id, tenant_id, username, password, nickname, user_type, status) VALUES
(1, NULL, 'admin@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '平台管理员', 'PLATFORM', 'ENABLED'),
(2, 1, 'ent@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '企业管理员', 'ENTERPRISE', 'ENABLED'),
(3, 1, 'user@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '参会用户', 'ATTENDEE', 'ENABLED');
