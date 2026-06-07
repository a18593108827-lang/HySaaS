# API 变更记录

格式：`日期 | 模块 | 变更说明`

---

## 2026-06-07 | 酒店协议

- `/enterprise/hotels` 酒店 CRUD + 房型配额抽屉
- API：`GET/POST /enterprise/hotels`、`GET/PUT/DELETE /enterprise/hotels/{id}`
- 房型：`GET/POST /enterprise/hotels/{hotelId}/room-types`、`PUT/DELETE .../room-types/{id}`
- 文档：`docs/features/hotel.md`

## 2026-06-05（七）| 邀请参会

- 活动列表操作列新增「邀请参会」弹窗 `InviteAttendeesDialog`
- 选人邀请：`GET /enterprise/attendees?nickname=` + `POST /enterprise/events/{id}/invites`
- 邀请链接：`POST /enterprise/events/{id}/invite-link` → `/portal/events/{id}/register?inviteToken=`
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
