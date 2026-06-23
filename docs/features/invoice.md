# 发票（参会申请 + 企业管理）

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/orders` |
| ent@test.com | 企业 | `/enterprise/finance/invoices` |

密码：`123456`。

> 契约源码：`frontend/src/api/portal.ts`、`frontend/src/api/enterprise.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/portal/OrdersView.vue`、`frontend/src/views/enterprise/FinanceInvoicesView.vue`  
> 支付流程：[payment.md](./payment.md)

## 业务流程

```
订单 PAID → 参会人申请发票 → 开票（当前 mock / 待接票点云 / 待接微信发票助手）
                                    ↓
                          回调落库 → 邮件发下载链接
                                    ↓
                          企业端 /enterprise/finance/invoices 查看（下载待接）
```

## 参会端 `/portal/orders`

| 操作 | 条件 | API |
|------|------|-----|
| 去支付 | `PENDING` | `POST /portal/pay/create` |
| 申请发票 | `PAID` 且未申请 | 弹窗 `POST /portal/invoices/apply` |
| 刷新 | — | `GET /portal/orders` |

申请字段：发票抬头、税号、接收邮箱。提交后 `invoiceStatus=APPLYING`。

| invoiceStatus | 说明 |
|---------------|------|
| NONE | 未申请（可申请） |
| APPLYING | 开票中 |
| ISSUED | 已开票 |

## 企业端 `/enterprise/finance/invoices`

只读列表，查看本租户开票记录（后续补下载）。

| 操作 | API |
|------|-----|
| 刷新 | `GET /enterprise/finance/invoices` |

---

## API 契约

### 参会端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/portal/orders` | Query: `page?` |
| POST | `/portal/pay/create` | `{ bizType, bizId }` → `{ payUrl }` |
| POST | `/portal/invoices/apply` | `InvoiceApplyPayload` |

### 企业端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/finance/invoices` | 本租户发票列表 |

### 回调（无需登录）

| Method | Path | 说明 |
|--------|------|------|
| POST | `/invoice/callback` | 票点云开票回调，body: `{ invoiceId, fileUrl?, status? }` |

### PayOrder

```ts
{
  id: number
  orderNo: string
  bizType: 'REGISTRATION' | 'HOTEL'
  amount: number
  status: 'PENDING' | 'PAID' | 'CLOSED' | 'CANCELLED'
  invoiceStatus?: 'NONE' | 'APPLYING' | 'ISSUED'
  createdAt: string
}
```

### InvoiceApplyPayload

```ts
{
  orderId: number
  title: string
  taxNo: string
  email: string
}
```

---

## 后端实现

| 后端 | 说明 |
|------|------|
| PortalInvoiceController | `POST /portal/invoices/apply` |
| InvoiceCallbackController | `POST /invoice/callback` |
| EnterpriseFinanceController | `GET /enterprise/finance/invoices` |
| InvoiceService | 校验 PAID + 不可重复申请 |
| InvoiceAsyncService | DEV mock 异步开票（1.5s 后回调） |
| EmailService | 开票完成发 `INVOICE_READY` 邮件 |
| `inv_invoice` | 抬头、税号、金额、status、file_url |

---

## 当前实现

| 项 | 状态 |
|----|------|
| 参会申请 | 已实现 |
| 票点云真实 API | 未接，`InvoiceAsyncService.mockIssue` 模拟 |
| 企业端下载 | UI 有按钮，未联调 `fileUrl` |
| 平台配置 | `invoiceAppKey` / `invoiceAppSecret`（票点云，待用） |

---

## 后续：票点云真实对接（未实现）

- 申请时调票点云开票 API，替换 `mockIssue`
- 回调 `POST /invoice/callback` 接收真实 `fileUrl`
- 企业端下载、`GET /enterprise/finance/invoices` 返回 `fileUrl`

---

## 后续：微信发票助手（未实现）

规划见 [全栈计划 Phase 7+](../../hysaas_全栈开发_c9f3f140.plan.md)。

| 项 | 说明 |
|----|------|
| 配置 | `/admin/config` 扩展微信发票助手授权信息 |
| 渠道 | `inv_invoice.channel`：`PIAODIAN` / `WECHAT` |
| 申请 | 参会端可选开票渠道；微信侧创建发票卡券 |
| 回调 | `POST /invoice/wechat/callback` 或微信消息通知 |
| 领取 | 微信卡包 / 链接领取；邮件 `INVOICE_READY` 含微信领取说明 |
| 企业端 | 列表展示渠道；微信发票支持同步状态查询 |
