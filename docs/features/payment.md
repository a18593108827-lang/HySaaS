# 支付

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/orders` |
| ent@test.com | 企业 | `/enterprise/finance/orders` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/portal.ts`、`frontend/src/api/enterprise.ts`  
> 页面源码：`portal/OrdersView.vue`、`enterprise/FinanceOrdersView.vue`  
> 发票：[invoice.md](./invoice.md)  
> 酒店预订：[hotel.md](./hotel.md)

## 业务流程

```
业务下单（报名/订房）→ 创建 pay_order(PENDING)
                    → 参会人 POST /portal/pay/create
                    → mock 支付 / 支付宝
                    → PAID → 联动业务（房单 LOCKED 等）
                    → 超时未支付 → XXL-JOB 关单(CLOSED)
```

| 订单 status | 说明 |
|-------------|------|
| PENDING | 待支付 |
| PAID | 已支付 |
| CLOSED | 超时关单 |
| CANCELLED | 已取消 |

| bizType | 说明 |
|---------|------|
| REGISTRATION | 报名费 |
| HOTEL | 酒店预订 |

---

## API 契约

### 参会端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/portal/orders` | Query: `page?`, `size?` |
| POST | `/portal/pay/create` | `{ bizType, bizId }` → `{ payUrl }` |
| POST | `/portal/pay/mock/{orderId}` | DEV mock 支付（需登录） |

`bizId` 为 **订单 id**（非业务单 id）。

### 企业端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/finance/orders` | Query: `page?`, `size?` |

### 回调（无需登录）

| Method | Path | 说明 |
|--------|------|------|
| POST | `/pay/alipay/notify` | 支付宝异步通知，form 参数 |

---

## 定时任务

| XXL-JOB 名称 | 说明 |
|--------------|------|
| `payOrderCloseJob` | 关闭超过 30 分钟未支付的 PENDING 订单；HOTEL 订单同步取消房单 |

管理台地址：`http://localhost:8088/xxl-job-admin`（docker-compose）

---

## 后端实现

| 后端 | 说明 |
|------|------|
| PortalPayController | 订单列表、创建支付、mock 支付 |
| EnterpriseFinanceController | 企业订单列表 |
| AlipayNotifyController | 支付宝回调，Redisson 分布式锁防重 |
| PayOrderService | 下单、支付、关单、业务联动 |
| PayOrderCloseJob | `@XxlJob("payOrderCloseJob")` |
| `pay_order` / `pay_transaction` | 订单与流水 |
