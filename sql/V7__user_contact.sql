USE hysaas;

ALTER TABLE sys_user
    ADD COLUMN email VARCHAR(128) NULL AFTER username,
    ADD COLUMN phone VARCHAR(32) NULL AFTER email;

UPDATE sys_user SET email = username WHERE username LIKE '%@%';
UPDATE sys_user SET phone = username WHERE username REGEXP '^1[0-9]{10}$';

UPDATE sys_user SET phone = '13800000001' WHERE id = 1 AND phone IS NULL;
UPDATE sys_user SET phone = '13800000002' WHERE id = 2 AND phone IS NULL;
UPDATE sys_user SET phone = '13800000003' WHERE id = 3 AND phone IS NULL;
UPDATE sys_user SET phone = '13800000004' WHERE id = 910010 AND phone IS NULL;

ALTER TABLE sys_user ADD UNIQUE KEY uk_phone_deleted (phone, deleted);
