# 支付

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/orders` |
| ent@test.com | 企业 | `/enterprise/finance/orders` |

密码：`123456`。

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
| POST | `/portal/pay/create` | `{ bizType, bizId, channel? }` → `{ payMode, payForm? \| payUrl? }` |
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

---

## 当前渠道

| payMode | 条件 | 说明 |
|---------|------|------|
| MOCK | `hysaas.pay.mock-enabled=true` | 调 `POST /portal/pay/mock/{orderId}` |
| ALIPAY | mock 关闭且支付宝已配置 | 返回 HTML form，跳转支付宝 |

平台配置项见 [platform.md](./platform.md) 全局配置。

---

## 后续：微信支付（未实现）

规划见 [全栈计划 Phase 7+](../../hysaas_全栈开发_c9f3f140.plan.md)。

| 项 | 说明 |
|----|------|
| 配置 | `/admin/config` 扩展微信商户号、AppId、APIv3 密钥、证书 |
| 订单 | `pay_order.pay_channel`：`ALIPAY` / `WECHAT` / `MOCK` |
| API | `POST /portal/pay/create` 增加 `channel: wechat`；`payMode=WECHAT` |
| 回调 | `POST /pay/wechat/notify`（验签 + 幂等，同支付宝） |
| 场景 | JSAPI（微信内）、Native（扫码）、H5（外链） |
| 前端 | `utils/pay.ts` 按 UA / 参数选择渠道；订单页展示微信支付 |
| 关单 | `payOrderCloseJob` 渠道无关，继续关 PENDING 单 |
