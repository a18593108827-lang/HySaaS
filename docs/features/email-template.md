# 邮件模板

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/email-templates` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`  
> 页面源码：`frontend/src/views/enterprise/EmailTemplatesView.vue`  
> SMTP 配置：[platform.md](./platform.md)

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 邮件模板 | `/enterprise/email-templates` | EmailTemplatesView |

**范围**：仅本租户默认模板（`event_id` 为空）；活动级覆盖后续扩展。

## 页面行为

| 操作 | API |
|------|-----|
| 列表 | `GET /enterprise/email-templates` |
| 编辑 | 弹窗 `PUT /enterprise/email-templates/{id}` |

支持变量：`{{name}}`、`{{eventName}}`、`{{status}}`、`{{orderNo}}`、`{{amount}}`、`{{fileUrl}}`

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/email-templates` | 模板列表 |
| PUT | `/enterprise/email-templates/{id}` | body: `{ content: string }` |

### EmailTemplate

```ts
{
  id: number
  code: string
  name: string
  content: string
}
```

### 模板 code 与触发时机

| code | 名称 | 触发时机 |
|------|------|----------|
| PAPER_SUBMITTED | 投稿提交 | 稿件提交 |
| PAPER_UNDER_REVIEW | 进入评审 | 分配专家 |
| PAPER_ACCEPTED | 录用通知 | 终审录用 |
| PAPER_REJECTED | 拒稿通知 | 终审拒稿 |
| PAPER_REVISION | 需修改通知 | 终审需修改 |
| REG_APPROVED | 报名通过 | 报名审核通过 |
| REG_REJECTED | 报名拒绝 | 报名审核拒绝 |
| PAY_SUCCESS | 支付成功 | 订单支付成功 |
| INVOICE_READY | 发票就绪 | 开票完成 |

---

## 发送机制

```
业务触发 → EmailTemplateService.render(code, vars)
         → JavaMailSender（读取 /admin/config 中 SMTP）
         → 失败入 RocketMQ topic hysaas-email-retry 重试
```

SMTP 未配置时跳过发送并打日志，不阻断主流程。

---

## 后端实现

| 后端 | 说明 |
|------|------|
| EnterpriseEmailTemplateController | 列表 + 编辑 |
| EmailTemplateService | 模板解析、租户默认模板初始化 |
| EmailService | SMTP 发送 + MQ 重试 |
| EmailSendConsumer | 消费 `hysaas-email-retry` |
| `msg_email_template` | 按 `tenant_id` + `code` 隔离 |
