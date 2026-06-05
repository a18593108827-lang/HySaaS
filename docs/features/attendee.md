# 参会账号与报名

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| user@test.com | 参会 | `/portal/events` |
| ent@test.com | 企业 | `/enterprise/attendees` |

密码任意 ≥6 位（DEV）。

> 契约：`frontend/src/api/enterprise.ts`  
> 页面：`AttendeesView.vue`、`AttendeeDetailView.vue`  
> 活动报名：`EventRegistrationsView.vue`（见 [event.md](./event.md)）

## 三类用户分工

| 类型 | 管理入口 | 说明 |
|------|----------|------|
| ENTERPRISE 企业成员 | `/enterprise/members` | 会务/财务/专家等内部账号，带 RBAC 角色 |
| ATTENDEE 参会账号 | `/enterprise/attendees` | 登录参会端的账号，仅本租户可见 |
| 活动报名记录 | `/enterprise/events/:id/registrations` | 某场活动的报名表单，审核通过后可签到 |

**隔离**：企业端接口不传 `tenantId`，后端从 Token 取当前租户；只能看本企业数据。

**平台**：`/admin/users` 可跨租户管理三类账号；参会仍无 C 端自助注册。

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 参会账号 | `/enterprise/attendees` | AttendeesView |
| 账号详情 | `/enterprise/attendees/:id` | AttendeeDetailView |

## 页面行为

### 列表 `/enterprise/attendees`

| 操作 | API |
|------|-----|
| 详情 | `/enterprise/attendees/{id}` |
| 编辑 | `PUT /enterprise/attendees/{id}` |
| 删除 | `DELETE /enterprise/attendees/{id}` |
| 新建 | `POST /enterprise/attendees` |
| 刷新 | `GET /enterprise/attendees` |

新建字段：账号、昵称、密码(≥6)、状态。

### 与活动报名的关系

```
企业新建 ATTENDEE 账号 → 参会人登录 /portal
                      → 自行报名 或 企业在活动列表「邀请参会」
                      → evt_registration 关联 user_id + event_id
                      → 企业在「报名审核」查看（邀请可配置免审）
                      → 扫码 / 手动签到
```

| 入口 | source |
|------|--------|
| 参会端自助报名 | SELF |
| 活动列表选人邀请 | INVITE |
| 邀请链接 | INVITE_LINK |

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/attendees` | Query: `nickname?`, `page?`, `size?` |
| GET | `/enterprise/attendees/{id}` | 单条 |
| POST | `/enterprise/attendees` | 新建 `EnterpriseAttendeePayload` |
| PUT | `/enterprise/attendees/{id}` | 编辑 |
| DELETE | `/enterprise/attendees/{id}` | 删除 |

### EnterpriseAttendee

```ts
{
  id: number
  username: string
  nickname: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}
```

### EnterpriseAttendeePayload

```ts
{
  username: string
  nickname: string
  password?: string
  status?: 'ENABLED' | 'DISABLED'
}
```

---

## 后端实现建议

| 后端 | 说明 |
|------|------|
| EnterpriseAttendeeController | 同 EnterpriseMemberController 模式 |
| `sys_user` | `user_type=ATTENDEE`，自动绑定当前 `tenant_id` |
| `@SaCheckRole('ADMIN')` 或 EVENT_STAFF | 与会务权限一致 |
