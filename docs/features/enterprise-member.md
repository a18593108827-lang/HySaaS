# 企业成员管理

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/dashboard` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/enterprise/MembersView.vue`、`MemberDetailView.vue`

## 侧栏与路由

| 入口 | 路由 | 组件 |
|------|------|------|
| 成员管理 | `/enterprise/members` | MembersView |
| 成员详情 | `/enterprise/members/:id` | MemberDetailView |

**范围**：仅管理当前登录租户下的企业成员（`userType=ENTERPRISE`），后端按 `tenant_id` 隔离。

参会登录账号见 [attendee.md](./attendee.md)（`/enterprise/attendees`），与本页成员互不重叠。

## 页面行为

### 成员列表 `/enterprise/members`

| 操作 | API |
|------|-----|
| 详情 | 跳转 `/enterprise/members/{id}` |
| 编辑 | 弹窗 `PUT /enterprise/members/{id}` |
| 删除 | `DELETE /enterprise/members/{id}` |
| 新建 | 弹窗 `POST /enterprise/members` |
| 角色筛选 | `GET /enterprise/members?role=` |
| 刷新 | `GET /enterprise/members` |

操作列顺序：详情 → 编辑 → 删除

新建字段：账号、昵称、角色(多选)、密码(≥6)、状态。

### 成员详情 `/enterprise/members/:id`

- `GET /enterprise/members/{id}`
- 编辑：昵称、角色、密码、状态
- 删除、返回列表

### 角色（企业 RBAC）

| 代码 | 说明 |
|------|------|
| ADMIN | 管理员 |
| EVENT_STAFF | 会务 |
| FINANCE | 财务 |
| EXPERT | 专家（评审） |

单场活动授权（`sys_event_permission`）后续在后端/活动模块扩展，前端暂未单独配置页。

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/members` | Query: `role?`, `page?`, `size?` |
| GET | `/enterprise/members/{id}` | 单条成员 |
| POST | `/enterprise/members` | 新建 `EnterpriseMemberPayload` |
| PUT | `/enterprise/members/{id}` | 编辑 |
| DELETE | `/enterprise/members/{id}` | 删除 |

### EnterpriseMember

```ts
{
  id: number
  username: string
  nickname: string
  roles: string[]          // ADMIN | EVENT_STAFF | FINANCE | EXPERT
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}
```

### EnterpriseMemberPayload

```ts
{
  username: string
  nickname: string
  roles: string[]
  password?: string
  status?: 'ENABLED' | 'DISABLED'
}
```

---

## Demo 数据

| username | roles |
|----------|-------|
| ent@test.com | ADMIN |
| staff@test.com | EVENT_STAFF |
| expert@test.com | EXPERT |

---

## 后端实现建议

| 后端 | 前端入口 |
|------|----------|
| EnterpriseMemberController | MembersView、MemberDetailView |
| `sys_user` + `sys_user_role` | 自动绑定当前 tenant_id |
| `@SaCheckRole('ADMIN')` | 仅管理员可管理成员 |
