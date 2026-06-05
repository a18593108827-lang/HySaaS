# 平台管理模块（租户管理 + 全局配置 + 企业入驻）

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| admin@test.com | 平台 | `/admin/dashboard` |
| ent@test.com | 企业 | `/enterprise/dashboard` |
| user@test.com | 参会 | `/portal/events` |

密码任意 ≥6 位（仅 DEV 无后端时生效）。

> 契约源码：`frontend/src/api/admin.ts`、`frontend/src/api/auth.ts`、`frontend/src/api/public.ts`、`frontend/src/types/index.ts`  
> 页面源码：`frontend/src/views/admin/`、`frontend/src/views/public/TenantRegisterView.vue`、`frontend/src/views/login/LoginView.vue`

## 侧栏与路由

| 入口 | 路由 | 组件 | 说明 |
|------|------|------|------|
| 概览 | `/admin/dashboard` | DashboardView | 需 PLATFORM |
| 租户管理 | `/admin/tenants` | TenantsView | 列表 + CRUD |
| 租户详情 | `/admin/tenants/:id` | TenantDetailView | 详情 + 编辑/审核/删除 |
| 用户管理 | `/admin/users` | UsersView | 三类账号 CRUD |
| 用户详情 | `/admin/users/:id` | UserDetailView | 详情 + 编辑/删除 |
| 全局配置 | `/admin/config` | ConfigView | 需 PLATFORM |
| 企业入驻（公开） | `/register` | TenantRegisterView | 无需登录 |
| 登录 | `/login` | LoginView | 链到入驻申请 |

**导航规则**

- 三端隔离：仅 `userType=PLATFORM` 可访问 `/admin/*`
- `/register` 为公开页，已登录用户访问会 redirect 到对应端首页
- 登录页底部「企业入驻申请」→ `/register`

## 页面行为

### 平台概览 `/admin/dashboard`

| 展示 | API |
|------|-----|
| 待审核 / 已开通 / 租户总数 | `GET /admin/tenants`（pending/approved 仍为 demo，total 可联调） |
| 快捷入口 | 跳转租户管理、用户管理、全局配置 |

### 用户列表 `/admin/users`

| 操作 | 条件 | 行为 / API |
|------|------|------------|
| 详情 | — | 跳转 `/admin/users/{id}` |
| 编辑 | — | 弹窗 `PUT /admin/users/{id}` |
| 删除 | 二次确认 | `DELETE /admin/users/{id}` |
| 新建 | 弹窗 | `POST /admin/users` |
| 类型筛选 | PLATFORM / ENTERPRISE / ATTENDEE | `GET /admin/users?userType=` |
| 刷新 | — | `GET /admin/users` |

操作列顺序：详情 → 编辑 → 删除

新建字段：账号、昵称、类型、密码(≥6)、状态；企业用户必选所属租户（已 APPROVED 租户列表）。

编辑：可改昵称、状态、密码（留空不改）；账号和类型不可改。

**参会用户无自助注册**；企业可在 `/enterprise/attendees` 创建本租户 ATTENDEE，平台在 `/admin/users` 跨租户管理。

### 用户详情 `/admin/users/:id`

- `GET /admin/users/{id}` 加载
- 操作：编辑（昵称/状态/密码）、删除

### 租户列表 `/admin/tenants`

| 操作 | 条件 | 行为 / API |
|------|------|------------|
| 详情 | — | 跳转 `/admin/tenants/{id}` |
| 编辑 | — | 弹窗 `PUT /admin/tenants/{id}` |
| 通过 | 仅 `PENDING` | `PUT /admin/tenants/{id}/audit` `{ status: APPROVED }` |
| 拒绝 | 仅 `PENDING` | `PUT /admin/tenants/{id}/audit` `{ status: REJECTED }` |
| 删除 | 二次确认 | `DELETE /admin/tenants/{id}` |
| 新建 | 弹窗 | `POST /admin/tenants`（可选初始状态 APPROVED / PENDING） |
| 状态筛选 | — | `GET /admin/tenants?status=` |
| 刷新 | — | `GET /admin/tenants` |

操作列顺序：详情 → 编辑 → 通过 → 拒绝 → 删除

列表字段：企业名称、联系人、联系电话、状态、申请时间。

平台新建默认可选「已通过」或「待审核」；企业自助申请固定为 `PENDING`。

### 租户详情 `/admin/tenants/:id`

- `GET /admin/tenants/{id}` 加载，`el-descriptions` 展示完整信息
- 操作：编辑（弹窗）、通过/拒绝（仅 PENDING）、删除
- 「← 返回列表」→ `/admin/tenants`

### 企业入驻 `/register`

| 字段 | 必填 |
|------|------|
| 企业名称 | ✓ |
| 联系人 | ✓ |
| 联系电话 | ✓ |
| 联系邮箱 | ✓ |
| 企业地址 | — |
| 备注 | — |

提交 → `POST /public/tenant/apply`，成功后展示结果页，引导回登录。  
后端应创建 `sys_tenant`，`status=PENDING`。

### 全局配置 `/admin/config`

| 操作 | API |
|------|-----|
| 加载 | `GET /admin/config` |
| 保存 | `PUT /admin/config` |

字段：`smtpHost`、`smtpPort`、`smtpUser`、`smtpPass`、`alipayAppId`、`alipayPrivateKey`、`invoiceAppKey`、`invoiceAppSecret`

---

## API 契约

统一前缀 `/api`，响应：`{ "code": 0, "message": "ok", "data": ... }`

### 认证（三端共用）

| Method | Path | 说明 |
|--------|------|------|
| POST | `/auth/login` | `{ username, password }` → `{ token, userType }` |
| POST | `/auth/logout` | 登出 |
| GET | `/auth/me` | `UserInfo` |

DEV：登录失败时 admin→PLATFORM、ent→ENTERPRISE、其余→ATTENDEE。

### 公开 — 企业入驻

| Method | Path | 说明 |
|--------|------|------|
| POST | `/public/tenant/apply` | Body: `TenantApplyPayload`，无需 Token |

### 平台 — 租户

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/tenants` | Query: `status?`, `page?`, `size?` → `{ records, total }` |
| GET | `/admin/tenants/{id}` | 单条 `Tenant` |
| POST | `/admin/tenants` | 平台新建，body 含 name/contact/status 等 |
| PUT | `/admin/tenants/{id}` | 编辑信息（不含审核状态变更） |
| PUT | `/admin/tenants/{id}/audit` | `{ status: APPROVED \| REJECTED }` |
| DELETE | `/admin/tenants/{id}` | 删除租户 |

### 平台 — 用户

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/users` | Query: `userType?`, `tenantId?`, `page?`, `size?` |
| GET | `/admin/users/{id}` | 单条 `AdminUser` |
| POST | `/admin/users` | 新建，body: `AdminUserPayload` |
| PUT | `/admin/users/{id}` | 编辑 nickname/status/password |
| DELETE | `/admin/users/{id}` | 删除 |

### 平台 — 配置

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/config` | 全局配置对象 |
| PUT | `/admin/config` | 保存配置 |

---

## 数据模型

### Tenant

```ts
{
  id: number
  name: string
  contactName: string
  contactPhone: string
  contactEmail?: string
  address?: string
  remark?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
  updatedAt?: string
}
```

### TenantApplyPayload（入驻申请）

```ts
{ name, contactName, contactPhone, contactEmail, address?, remark? }
```

### AdminUser

```ts
{
  id: number
  username: string
  nickname: string
  userType: 'PLATFORM' | 'ENTERPRISE' | 'ATTENDEE'
  tenantId?: number
  tenantName?: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}
```

### AdminUserPayload（新建/编辑）

```ts
{
  username: string
  nickname: string
  userType: UserType
  tenantId?: number      // ENTERPRISE 必填
  password?: string      // 新建必填，编辑可选
  status?: 'ENABLED' | 'DISABLED'
}
```

### UserInfo（登录态）

```ts
{
  id: number
  username: string
  nickname: string
  userType: 'PLATFORM' | 'ENTERPRISE' | 'ATTENDEE'
  tenantId?: number
  roles: string[]
  eventPermissions: string[]
}
```

### 租户状态机

```
PENDING → APPROVED   （平台审核通过 / 平台直接新建为已通过）
PENDING → REJECTED   （平台拒绝）
```

---

## 入驻链路

```
企业 /register 提交
  → POST /public/tenant/apply
  → sys_tenant.status = PENDING

平台 /admin/tenants 审核通过
  → PUT /admin/tenants/{id}/audit { APPROVED }
  → 创建企业管理员账号，绑定 tenant_id
平台 /admin/users 审核通过租户后创建 ent 账号
  → 或平台手动新建 ENTERPRISE / ATTENDEE 用户

参会 user@test.com：平台创建 ATTENDEE 账号后登录
  → 无 /register 个人注册页
```

---

## Demo 数据

接口失败时列表/demo 详情：

| name | status |
|------|--------|
| 华东医学会 | PENDING |
| 深圳创新峰会 | APPROVED |

---

## 与 plan.md 差异

| 项 | plan.md | 当前前端 |
|----|---------|----------|
| 租户 API | GET/PUT 列表审核 | 完整 CRUD + 独立 audit 接口 |
| 入驻 | 隐含注册 | `/register` 公开申请页 |
| 概览统计 | — | pending/approved 仍为 demo |
| 参会注册 | 自助开号 | 仅平台创建用户，无 C 端注册页 |
| 用户管理 | plan 隐含 sys_user | `/admin/users` CRUD 已实现 |

---

## 后端实现建议

| 后端 | 前端入口 |
|------|----------|
| AuthController | LoginView、路由守卫 |
| PublicTenantController apply | TenantRegisterView |
| AdminTenantController CRUD/audit | TenantsView、TenantDetailView |
| AdminUserController CRUD | UsersView、UserDetailView |
| AdminConfigController | ConfigView |
| `sys_tenant` | Tenant / TenantApplyPayload VO |
| `sys_user` | AdminUser / AdminUserPayload VO |
