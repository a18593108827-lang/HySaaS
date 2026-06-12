USE hysaas;

INSERT INTO sys_role (id, tenant_id, code, name, created_at, updated_at, deleted) VALUES
(910001, 1, 'ADMIN', '管理员', NOW(), NOW(), 0),
(910002, 1, 'EVENT_STAFF', '会务', NOW(), NOW(), 0),
(910003, 1, 'FINANCE', '财务', NOW(), NOW(), 0),
(910004, 1, 'EXPERT', '专家', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_user_role (id, user_id, role_id, tenant_id) VALUES
(920001, 2, 910001, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
