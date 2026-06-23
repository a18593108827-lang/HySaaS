# API 变更记录

格式：`日期 | 模块 | 变更说明`

---

## 2026-06-23 | 文档同步 / 租户通知 / demo 清理

- 租户审核通过：自动建企业管理员 + `TENANT_APPROVED` 邮件（SMTP 未配置则跳过）
- 前端移除 DEV 假登录、评审/订单列表 API 失败 demo 数据
- 文档：`platform.md`、`hysaas_全栈开发_c9f3f140.plan.md` 同步现状；`payment.md` / `invoice.md` 补充微信支付、微信发票助手规划

## 2026-06-14 前

- 登录页底部：无活动上下文时「企业入驻申请」→ `/register`；有活动 redirect 时「活动报名」→ `/event/{id}/register`

## 2026-06-14 | 参会报名 / 投稿 / 支付

- 公开报名页 `/event/{id}/register`；C 端 `POST /public/attendee/register`
- 活动 `registrationFee`；报名返回 `payOrder`，bizType=REGISTRATION
- 投稿：草稿与投递分离；`GET /portal/submissions?scope=draft|submitted`
- 草稿 `POST /portal/submissions/draft`；PDF `POST /portal/submissions/{id}/file`
- 投递 `POST /portal/submissions/{id}/submit` 需 `{ eventId }`
- SQL：V8 报名费、V9 abstract_text、V10 event_id 可空
- 文档：`attendee.md`、`event.md`、`paper.md`

## 2026-06-12 | 后端 P6 发票/邮件/WebSocket

- 参会端 `POST /portal/invoices/apply`，票点云回调 `POST /invoice/callback`
- 企业端 `GET /enterprise/finance/invoices`、`GET/PUT /enterprise/email-templates`
- 邮件 SMTP 发送 + RocketMQ `hysaas-email-retry` 重试
- WebSocket `ws://{host}/api/ws/checkin/{eventId}` 签到人数推送
- 文档：`docs/features/invoice.md`、`email-template.md`，`event.md` 补 WebSocket

## 2026-06-12 | 后端 P5 酒店/支付

- 企业端酒店 CRUD、房单列表/核销、订单 `GET /enterprise/finance/orders`
- 参会端 `GET/POST /portal/hotels/{eventId}` 预订
- 支付 `POST /portal/pay/create`、`POST /portal/pay/mock/{orderId}`、`POST /pay/alipay/notify`
- XXL-JOB `payOrderCloseJob` 超时关单
- SQL V5 扩展 `hotel_booking`/`pay_order` 字段
- 文档：`docs/features/hotel.md`、`booking.md`、`payment.md`

## 2026-06-07（五）| 专家列表联调

- 稿件管理分配专家改调 `GET /enterprise/members?role=EXPERT`
- 仅展示 ENABLED 专家，无专家时提示去成员管理添加
- 文档：`docs/features/paper.md`

## 2026-06-07（四）| 活动开关

- 活动新建/编辑弹窗增加报名、论文、酒店开关
- 列表新增「功能」列展示已开启模块
- 文档：`docs/features/event.md`

## 2026-06-07（三）| 申请发票

- `/portal/orders` 已支付订单可申请发票弹窗
- API：`POST /portal/invoices/apply`（`InvoiceApplyPayload`）
- 订单增加 `invoiceStatus` 字段
- 文档：`docs/features/invoice.md`

## 2026-06-07（二）| 房单核销

- 新增 `/enterprise/bookings` 房单列表 + 核销
- API：`GET /enterprise/bookings`、`POST /enterprise/bookings/{id}/checkin`
- 文档：`docs/features/booking.md`

## 2026-06-07 | 酒店协议

- `/enterprise/hotels` 酒店 CRUD + 房型配额抽屉
- API：`GET/POST /enterprise/hotels`、`GET/PUT/DELETE /enterprise/hotels/{id}`
- 房型：`GET/POST /enterprise/hotels/{hotelId}/room-types`、`PUT/DELETE .../room-types/{id}`
- 文档：`docs/features/hotel.md`

## 2026-06-05（七）| 邀请参会

- 活动列表操作列新增「邀请参会」弹窗 `InviteAttendeesDialog`
- 选人邀请：`GET /enterprise/attendees?nickname=` + `POST /enterprise/events/{id}/invites`
- 邀请链接：`POST /enterprise/events/{id}/invite-link` → `/event/{id}/register?inviteToken=`
- 报名 `source`：SELF / INVITE / INVITE_LINK

## 2026-06-05（六）| 企业参会账号

- 新增 `/enterprise/attendees` 参会账号管理：CRUD + 详情
- API：`GET/POST /enterprise/attendees`、`GET/PUT/DELETE /enterprise/attendees/{id}`
- 仅本租户 `userType=ATTENDEE`；活动报名仍在 `/enterprise/events/:id/registrations`
- 文档：`docs/features/attendee.md`

## 2026-06-05（五）| 企业成员

- 新增 `/enterprise/members` 成员管理：CRUD + 详情
- API：`GET/POST /enterprise/members`、`GET/PUT/DELETE /enterprise/members/{id}`
- 角色：ADMIN / EVENT_STAFF / FINANCE / EXPERT

## 2026-06-05（四）| 平台用户

- 新增 `/admin/users` 用户管理：CRUD + 详情页
- API：`GET/POST /admin/users`、`GET/PUT/DELETE /admin/users/{id}`
- 支持 PLATFORM / ENTERPRISE / ATTENDEE 三类；企业用户绑定 tenantId
- 参会用户仍无自助注册，由平台创建

## 2026-06-05（三）| 平台租户

- 租户 CRUD：`POST/GET/PUT/DELETE /admin/tenants`，详情 `GET /admin/tenants/{id}`
- 审核独立：`PUT /admin/tenants/{id}/audit`（原 `PUT /admin/tenants/{id}` 仅 status 已废弃）
- 企业入驻：`POST /public/tenant/apply`，页面 `/register`
- 平台列表操作：详情、编辑、通过、拒绝、删除；详情页 `/admin/tenants/:id`
- Tenant 扩展：`contactEmail`、`address`、`remark`、`updatedAt`

## 2026-06-05 | 平台

- 新增 `docs/features/platform.md`
- 配置：`GET/PUT /admin/config`

## 2026-06-05（二）| 活动 UI

- 侧栏子菜单移除，「活动管理」单入口 `/enterprise/events`
- 报名审核 / 签到管理 / 获取二维码 改由列表操作列进入
- 二维码改为列表弹窗，无独立页面

## 2026-06-05（一）| 活动

- 新增 `DELETE /enterprise/events/{id}`
- `POST /enterprise/events/{id}/qrcode` 以 `token` 为主

## 初始契约

**企业端** `frontend/src/api/enterprise.ts`：

- `GET/POST /enterprise/events`
- `GET/PUT/DELETE /enterprise/events/{id}`
- `POST /enterprise/events/{id}/publish`
- `POST /enterprise/events/{id}/qrcode`
- `POST /enterprise/events/{id}/invites`
- `POST /enterprise/events/{id}/invite-link`
- `GET /enterprise/events/{eventId}/registrations`
- `PUT /enterprise/registrations/{id}`
- `GET /enterprise/events/{eventId}/checkin`

**参会端** `frontend/src/api/portal.ts`：

- `GET /portal/events/{id}?token=`
- `POST /portal/checkin/{eventId}`
- `GET /portal/orders`
- `POST /portal/pay/create`
- `POST /portal/invoices/apply`

**平台端** `frontend/src/api/admin.ts`、`auth.ts`、`public.ts`：

- `POST /auth/login`、`POST /auth/logout`、`GET /auth/me`
- `GET/POST /admin/tenants`
- `GET/PUT/DELETE /admin/tenants/{id}`
- `PUT /admin/tenants/{id}/audit`
- `GET/POST /admin/users`
- `GET/PUT/DELETE /admin/users/{id}`
- `GET/PUT /admin/config`
- `POST /public/tenant/apply`

**企业端** `frontend/src/api/enterprise.ts`（参会账号）：

- `GET/POST /enterprise/attendees`
- `GET/PUT/DELETE /enterprise/attendees/{id}`

**企业端** `frontend/src/api/enterprise.ts`（成员）：

- `GET/POST /enterprise/members`
- `GET/PUT/DELETE /enterprise/members/{id}`

**企业端** `frontend/src/api/enterprise.ts`（酒店）：

- `GET/POST /enterprise/hotels`
- `GET/PUT/DELETE /enterprise/hotels/{id}`
- `GET/POST /enterprise/hotels/{hotelId}/room-types`
- `PUT/DELETE /enterprise/hotels/{hotelId}/room-types/{id}`

**企业端** `frontend/src/api/enterprise.ts`（房单）：

- `GET /enterprise/bookings`
- `POST /enterprise/bookings/{id}/checkin`

**企业端** `frontend/src/api/enterprise.ts`（财务/邮件）：

- `GET /enterprise/finance/orders`
- `GET /enterprise/finance/invoices`
- `GET /enterprise/email-templates`
- `PUT /enterprise/email-templates/{id}`

**参会端** `frontend/src/api/portal.ts`（酒店/支付）：

- `GET /portal/hotels/{eventId}`
- `POST /portal/hotels/{eventId}/book`
- `POST /portal/pay/mock/{orderId}`

**回调**（无需登录）：

- `POST /pay/alipay/notify`
- `POST /invoice/callback`

**WebSocket**：

- `ws://{host}/api/ws/checkin/{eventId}`
