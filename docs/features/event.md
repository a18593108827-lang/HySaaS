# 活动模块（企业端 + 参会签到）

> 参会登录账号：`/enterprise/attendees`（见 [attendee.md](./attendee.md)）  
> 本模块「报名审核」管理的是单场活动的报名记录，不是账号 CRUD。

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| admin@test.com | 平台 | `/admin/dashboard` |
| ent@test.com | 企业 | `/enterprise/dashboard` |
| user@test.com | 参会 | `/portal/events` |

密码任意 ≥6 位（仅 DEV 无后端时生效）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/api/portal.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/enterprise/`、`frontend/src/components/CheckinQrcodeDialog.vue`、`frontend/src/views/portal/CheckinView.vue`

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 活动管理（单菜单，无子菜单） | `/enterprise/events` | EventsView |
| 报名审核（详情） | `/enterprise/events/:id/registrations` | EventRegistrationsView |
| 签到管理（详情） | `/enterprise/events/:id/checkin` | EventCheckinView |

**导航规则**

- 侧栏仅「活动管理」一项，点击进入活动列表
- 报名审核 / 签到管理 / 获取二维码：均在列表「操作」列入口，无独立侧栏子菜单
- 进入详情页后顶部展示活动名，「← 返回活动列表」回到 `/enterprise/events`

## 页面行为

### 活动列表 `/enterprise/events`

| 操作 | 条件 | 行为 / API |
|------|------|------------|
| 报名审核 | — | 跳转 `/enterprise/events/{id}/registrations` |
| 签到管理 | — | 跳转 `/enterprise/events/{id}/checkin` |
| 获取二维码 | — | 弹窗 `CheckinQrcodeDialog` |
| 邀请参会 | — | 弹窗 `InviteAttendeesDialog` |
| 发布 | 仅 `DRAFT` | `POST /enterprise/events/{id}/publish` |
| 编辑 | — | 弹窗 `PUT /enterprise/events/{id}` |
| 删除 | 二次确认 | `DELETE /enterprise/events/{id}` |
| 新建 | 弹窗 | `POST /enterprise/events` |
| 刷新 | — | `GET /enterprise/events` |

操作列顺序：报名审核 → 签到管理 → 获取二维码 → 邀请参会 → 发布 → 编辑 → 删除

跳转详情时：`eventStore.setActiveEventId(id)` + `setCurrent(row)`；列表加载后 `setList(records)` 供详情页标题回退。

新建/编辑弹窗字段：名称、地点、开始/结束时间、开放功能（报名/论文/酒店开关）。

创建成功后默认 `status=DRAFT`，三个开关默认 `false`。开启后参会端 `/portal/events` 显示对应入口。

### 报名审核 `/enterprise/events/:id/registrations`

- 父级 `EventDetailLayout` 展示活动标题（优先 `GET /enterprise/events/{id}`，失败读 store 缓存）
- 状态筛选：PENDING / APPROVED / REJECTED
- 待审核：通过 / 拒绝 → `PUT /enterprise/registrations/{id}`，提示邮件已发送

### 签到管理 `/enterprise/events/:id/checkin`

- 已签到 / 应到 / 签到率 + 明细表
- 15s 轮询 `GET /enterprise/events/{eventId}/checkin`

### 获取二维码（弹窗，无独立路由）

- 列表点击「获取二维码」→ `CheckinQrcodeDialog`
- 打开时 `POST /enterprise/events/{id}/qrcode` 取 token，前端 `qrcode` 库生成 PNG
- 支持复制链接、下载二维码

### 邀请参会（弹窗，无独立路由）

**Tab 1 — 选人邀请**

- 按昵称/账号模糊搜索本租户参会账号：`GET /enterprise/attendees?nickname=`
- 多选 ENABLED 用户 → `POST /enterprise/events/{id}/invites`
- Body：`{ userIds: number[], autoApprove?: boolean }`
- `autoApprove=false`（默认）→ 生成 PENDING 报名 + 发通知；`true` → 直接 APPROVED
- 已报名同活动用户跳过（返回 `skipped`）

**Tab 2 — 邀请链接**

- `POST /enterprise/events/{id}/invite-link` → `{ token?, inviteUrl? }`
- 链接：`/portal/events/{id}/register?inviteToken=xxx`（与签到 token 分离）
- 支持复制链接、展示报名二维码

### 参会扫码签到 `/checkin/:eventId?token=xxx`

- 独立页，需 `ATTENDEE` 账号
- 带 token：未登录 → login + redirect；已登录 → 自动 `POST /portal/checkin/{eventId}`
- 无 token：手动「确认签到」

---

## API 契约

统一前缀 `/api`，响应：`{ "code": 0, "message": "ok", "data": ... }`  
axios 拦截器：`code` 为 0 或 200 时返回 `data`。

### 企业端 — 活动

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/events` | 分页列表，`{ records, total }` |
| GET | `/enterprise/events/{id}` | 单条 `EventItem` |
| POST | `/enterprise/events` | 创建，body: title(必填), location, startTime, endTime, registrationEnabled?, paperEnabled?, hotelEnabled? |
| PUT | `/enterprise/events/{id}` | 更新（含三个开关） |
| DELETE | `/enterprise/events/{id}` | 删除 |
| POST | `/enterprise/events/{id}/publish` | 发布 DRAFT→PUBLISHED |
| POST | `/enterprise/events/{id}/qrcode` | 返回 `{ token?, checkinUrl?, qrcodeUrl? }`，前端以 token 为主 |
| POST | `/enterprise/events/{id}/invites` | 批量邀请，`EventInvitePayload` → `EventInviteResult` |
| POST | `/enterprise/events/{id}/invite-link` | 生成报名邀请链接 token |

### 企业端 — 报名 / 签到

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/events/{eventId}/registrations?status=` | 报名列表 |
| PUT | `/enterprise/registrations/{id}` | `{ status: APPROVED \| REJECTED }` |
| GET | `/enterprise/events/{eventId}/checkin` | `{ count?, total?, records? }` |

### 参会端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/portal/events/{id}?token=` | 活动详情 |
| POST | `/portal/events/{id}/register` | 提交报名，body 可含 `inviteToken` |
| POST | `/portal/checkin/{eventId}` | body `{ token? }`，扫码必传 token |

---

## 数据模型

见 `frontend/src/types/index.ts`：`EventItem`、`Registration`、`CheckinQrcodeResult`、`EventInvitePayload`、`EventInviteResult`、`EventInviteLinkResult`。

`Registration.source`：`SELF` | `INVITE` | `INVITE_LINK`

活动状态：`DRAFT → PUBLISHED → REGISTRATION_OPEN → REGISTRATION_CLOSED`  
前端列表仅暴露「发布」（DRAFT）。

---

## 签到二维码链路

```
列表「获取二维码」
  → POST /enterprise/events/{id}/qrcode
  → token 存 localStorage checkin-token-{eventId}
  → URL: {origin}/checkin/{eventId}?token={token}
  → 参会人扫码 → 登录 → POST /portal/checkin/{eventId}
```

Demo：接口失败时 localStorage 自生成 token，前端本地画图。

---

## 邀请参会链路

```
列表「邀请参会」
  Tab1 选人 → GET /enterprise/attendees?nickname=
           → POST /enterprise/events/{id}/invites
           → evt_registration(user_id, event_id, source=INVITE)
  Tab2 链接 → POST /enterprise/events/{id}/invite-link
           → /portal/events/{id}/register?inviteToken=
           → POST /portal/events/{id}/register（带 inviteToken）
           → evt_registration(source=INVITE_LINK)
```

---

## 与 plan.md 差异

| 项 | plan.md | 当前前端 |
|----|---------|----------|
| 导航 | 活动子菜单 | 单菜单 + 列表操作列 |
| 二维码 | 独立页 / 详情内 | 列表弹窗 |
| 删除 | 未写 | DELETE 已接 |
| 活动名 | — | store 缓存 + getEvent 双保险 |

---

## 后端实现建议

| 后端 | 前端入口 |
|------|----------|
| EventController CRUD/publish/delete | EventsView |
| EventQrcodeController generate | CheckinQrcodeDialog |
| EventInviteController | InviteAttendeesDialog |
| RegistrationController | EventRegistrationsView |
| CheckinController (enterprise) | EventCheckinView |
| PortalCheckinController | CheckinView |
