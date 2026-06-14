# 参会账号与报名

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/events` |
| ent@test.com | 企业 | `/enterprise/attendees` |

密码任意 ≥6 位（DEV）。

> 契约：`frontend/src/api/enterprise.ts`、`frontend/src/api/public.ts`、`frontend/src/api/portal.ts`  
> 页面：`AttendeesView.vue`、`public/EventRegisterView.vue`、`login/LoginView.vue`  
> 活动报名审核：`EventRegistrationsView.vue`（见 [event.md](./event.md)）

## 三类用户分工

| 类型 | 管理入口 | 说明 |
|------|----------|------|
| ENTERPRISE 企业成员 | `/enterprise/members` | 会务/财务/专家等内部账号，带 RBAC 角色 |
| ATTENDEE 参会账号 | `/enterprise/attendees` | 企业可预建；也可 C 端自助注册 |
| 活动报名记录 | `/enterprise/events/:id/registrations` | 某场活动的报名记录，审核通过后可订酒店/签到 |

**租户模型（当前）**：一个参会账号绑定一个企业 `tenant_id`；可报名该企业下**多个活动**，不可跨企业复用同一邮箱。

## 参会人注册与登录

| 入口 | 路由 | 说明 |
|------|------|------|
| 登录 | `/login` | 邮箱或手机 + 密码 |
| 公开活动报名 | `/event/:id/register` | 无需登录；可注册并报名 |
| 企业入驻 | `/register` | 租户申请，与参会无关 |

**公开报名页** `/event/:id/register`

- 未登录：填表 + 设密码 → `POST /public/attendee/register` → 自动登录 → 提交报名
- 已登录参会人：直接填表报名
- 支持 `?inviteToken=` 邀请链接

**登录页**：从 redirect 解析活动 id 时，底部显示「活动报名」链到公开报名页。

## 企业端 — 参会账号

| 入口 | 路由 | 组件 |
|------|------|------|
| 参会账号 | `/enterprise/attendees` | AttendeesView |
| 账号详情 | `/enterprise/attendees/:id` | AttendeeDetailView |

| 操作 | API |
|------|-----|
| 列表/搜索 | `GET /enterprise/attendees?nickname=` |
| 新建 | `POST /enterprise/attendees` |
| 编辑 | `PUT /enterprise/attendees/{id}` |
| 删除 | `DELETE /enterprise/attendees/{id}` |

新建字段：邮箱、手机、昵称、密码(≥6)、状态。

## 报名流程

```
方式 A：公开链接 /event/{id}/register
  → 注册/登录 → POST /portal/events/{id}/register
  → 有报名费则支付 → 企业审核

方式 B：企业预建账号 → 登录 /portal/events → 报名

方式 C：企业「邀请参会」→ source=INVITE / INVITE_LINK
```

| 入口 | source |
|------|--------|
| 参会端自助报名 | SELF |
| 活动列表选人邀请 | INVITE |
| 邀请链接 | INVITE_LINK |

审核通过后：付费会员/理事会成员可订酒店；所有已通过者可签到。

---

## API 契约

### 公开（无需登录）

| Method | Path | 说明 |
|--------|------|------|
| POST | `/public/attendee/register` | `{ eventId, nickname, email, phone, password }` → 注册并返回 token |

### 企业端

| Method | Path | 说明 |
|--------|------|-----|
| GET | `/enterprise/attendees` | Query: `nickname?`, `page?`, `size?` |
| GET | `/enterprise/attendees/{id}` | 单条 |
| POST | `/enterprise/attendees` | 新建 |
| PUT | `/enterprise/attendees/{id}` | 编辑 |
| DELETE | `/enterprise/attendees/{id}` | 删除 |

### 参会端报名

| Method | Path | 说明 |
|--------|------|-----|
| GET | `/portal/events/{id}` | 活动详情（GET 可未登录，见 SaToken 白名单） |
| POST | `/portal/events/{id}/register` | 返回 `{ registration, payOrder? }` |

---

## 后端实现

| 后端 | 说明 |
|------|------|
| PublicAttendeeController | C 端注册 |
| PublicAttendeeRegisterService | 按 eventId 解析 tenant，创建 ATTENDEE |
| RegistrationService | 报名、邀请、审核校验 |
| UserContactService | 邮箱/手机全局唯一 |
| SQL V7 | 用户联系方式字段 |
