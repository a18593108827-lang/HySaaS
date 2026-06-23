# 邮件模板

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/email-templates` |
| admin@test.com | 平台 | `/admin/config`（SMTP 测试） |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/api/admin.ts`  
> 页面源码：`frontend/src/views/enterprise/EmailTemplatesView.vue`、`frontend/src/views/admin/ConfigView.vue`  
> SMTP 配置：[platform.md](./platform.md)

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 邮件模板 | `/enterprise/email-templates` | EmailTemplatesView |
| 全局配置 | `/admin/config` | ConfigView（SMTP + 测试发信） |

**范围**：租户默认模板（`event_id` 为空）；可选活动覆盖（`event_id` 有值时优先于默认）。

## 页面行为

| 操作 | API |
|------|-----|
| 模板列表 | `GET /enterprise/email-templates?eventId=` |
| 编辑 | 弹窗 `PUT /enterprise/email-templates/{id}` body: `{ subject?, content? }` |
| 企业发信记录 | `GET /enterprise/email-templates/logs` |
| SMTP 测试 | `POST /admin/config/test-email` body: `{ to }` |
| 平台发信记录 | `GET /admin/config/email-logs` |

支持变量：`{{name}}`、`{{eventName}}`、`{{status}}`、`{{orderNo}}`、`{{amount}}`、`{{fileUrl}}`

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/email-templates` | Query: `eventId?` |
| PUT | `/enterprise/email-templates/{id}` | 更新主题/正文 |
| GET | `/enterprise/email-templates/logs` | 本租户发信记录 |
| POST | `/admin/config/test-email` | SMTP 测试 |
| GET | `/admin/config/email-logs` | 全平台发信记录 |

### EmailTemplate

```ts
{
  id: number
  code: string
  name: string
  subject: string
  content: string
  eventId?: number
}
```

### 模板 code 与触发时机

| code | 名称 | 触发时机 |
|------|------|----------|
| PAPER_SUBMITTED | 投稿提交 | 首次投稿 / 修改重投 |
| PAPER_UNDER_REVIEW | 进入评审 | 分配专家 |
| PAPER_ACCEPTED | 录用通知 | 终审录用 |
| PAPER_REJECTED | 拒稿通知 | 终审拒稿 |
| PAPER_REVISION | 需修改通知 | 终审需修改 |
| REG_APPROVED | 报名通过 | 审核通过 / 邀请自动通过 |
| REG_REJECTED | 报名拒绝 | 报名审核拒绝 |
| PAY_SUCCESS | 支付成功 | 订单支付成功 |
| INVOICE_READY | 发票就绪 | 开票回调（含 mock） |

---

## 发送机制

```
业务触发 → EmailTemplateService.resolve（活动级优先）
         → JavaMailSender（/admin/config SMTP，465 SSL / 587 STARTTLS）
         → 成功/失败写入 msg_email_log
         → 失败入 RocketMQ hysaas-email-retry（最多 3 次）
         → 超限入 hysaas-email-dead 死信
```

SMTP 未配置时跳过发送并打日志，不阻断主流程。

---

## 后端实现

| 后端 | 说明 |
|------|------|
| EnterpriseEmailTemplateController | 模板 CRUD + 企业发信记录 |
| AdminConfigController | SMTP 配置 + 测试发信 + 平台发信记录 |
| EmailTemplateService | 租户/活动模板初始化与解析 |
| EmailService | SMTP 发送 + 重试 + 死信 |
| EmailLogService | `msg_email_log` 记录 |
| EmailSendConsumer | 消费 `hysaas-email-retry` |
| EmailDeadLetterConsumer | 消费 `hysaas-email-dead` |

## 数据库迁移

- `V11__inv_invoice_email.sql`：`inv_invoice.email`
- `V12__email_log.sql`：`msg_email_log`
