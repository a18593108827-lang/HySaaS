# 发票（参会申请 + 企业管理）

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/orders` |
| ent@test.com | 企业 | `/enterprise/finance/invoices` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/portal.ts`、`frontend/src/api/enterprise.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/portal/OrdersView.vue`、`frontend/src/views/enterprise/FinanceInvoicesView.vue`

## 业务流程

```
订单 PAID → 参会人申请发票 → 票点云开票 → 回调落库 → 邮件发下载链接
                                    ↓
                          企业端 /enterprise/finance/invoices 查看
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
| GET | `/enterprise/finance/invoices` | Query: `page?` |

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

## 后端实现建议

| 后端 | 说明 |
|------|------|
| PortalInvoiceController | `POST /portal/invoices/apply` |
| 校验 | 订单须 `PAID` 且归属当前用户，不可重复申请 |
| 票点云 | 异步开票，回调更新 `inv_invoice` |
| 邮件 | `INVOICE_READY` 模板发下载链接 |
