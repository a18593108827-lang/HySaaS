USE hysaas;

INSERT INTO sys_user (id, tenant_id, username, password, nickname, user_type, status, created_at, updated_at, deleted) VALUES
(910010, 1, 'expert@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '评审专家王', 'ENTERPRISE', 'ENABLED', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO sys_user_role (id, user_id, role_id, tenant_id) VALUES
(920010, 910010, 910004, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
