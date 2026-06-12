USE hysaas;

INSERT INTO msg_email_template (id, tenant_id, event_id, code, subject, body, created_at, updated_at) VALUES
(1001, 1, NULL, 'PAPER_SUBMITTED', '投稿提交通知', '尊敬的 {{name}}，您的论文已提交至 {{eventName}}。', NOW(), NOW()),
(1002, 1, NULL, 'PAPER_UNDER_REVIEW', '进入评审通知', '尊敬的 {{name}}，您的论文 {{eventName}} 已进入评审。', NOW(), NOW()),
(1003, 1, NULL, 'PAPER_ACCEPTED', '录用通知', '尊敬的 {{name}}，恭喜！您的论文 {{eventName}} 已录用。', NOW(), NOW()),
(1004, 1, NULL, 'PAPER_REJECTED', '拒稿通知', '尊敬的 {{name}}，很遗憾，您的论文 {{eventName}} 未通过评审。', NOW(), NOW()),
(1005, 1, NULL, 'PAPER_REVISION', '需修改通知', '尊敬的 {{name}}，您的论文 {{eventName}} 需要修改后重新提交。', NOW(), NOW()),
(1006, 1, NULL, 'REG_APPROVED', '报名通过', '尊敬的 {{name}}，您已成功报名 {{eventName}}。', NOW(), NOW()),
(1007, 1, NULL, 'REG_REJECTED', '报名拒绝', '尊敬的 {{name}}，您的 {{eventName}} 报名未通过审核。', NOW(), NOW()),
(1008, 1, NULL, 'PAY_SUCCESS', '支付成功', '尊敬的 {{name}}，订单 {{orderNo}} 支付成功，金额 {{amount}} 元。', NOW(), NOW()),
(1009, 1, NULL, 'INVOICE_READY', '发票就绪', '尊敬的 {{name}}，发票已开具，请点击下载：{{fileUrl}}', NOW(), NOW());
