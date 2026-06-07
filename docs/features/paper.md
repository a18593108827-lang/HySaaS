# 论文评审

## 测试账号

| 账号 | 端 | 首页 |
|------|-----|------|
| ent@test.com | 企业 | `/enterprise/papers` |
| expert@test.com | 企业 | `/enterprise/reviews` |
| user@test.com | 参会 | `/portal/submissions` |

密码任意 ≥6 位（DEV）。

> 契约源码：`frontend/src/api/enterprise.ts`、`frontend/src/api/portal.ts`  
> 页面源码：`PapersView.vue`、`ReviewsView.vue`、`portal/SubmissionsView.vue`  
> 专家成员：[enterprise-member.md](./enterprise-member.md)

## 侧栏与路由

| 入口 | 路由 | 组件 | 角色 |
|------|------|------|------|
| 稿件管理 | `/enterprise/papers` | PapersView | 会务 |
| 评审工作台 | `/enterprise/reviews` | ReviewsView | 专家 |
| 我的投稿 | `/portal/submissions` | SubmissionsView | 参会 |

## 页面行为

### 稿件管理 `/enterprise/papers`

| 操作 | 条件 | API |
|------|------|-----|
| 分配专家 | SUBMITTED / RESUBMITTED | `GET /enterprise/members?role=EXPERT` + `POST /enterprise/papers/{id}/assign` |
| 录用/拒稿/需修改 | REVIEW_DONE | `PUT /enterprise/papers/{id}/finalize` |
| 刷新 | — | `GET /enterprise/papers` |

分配专家弹窗从本租户 `EXPERT` 角色成员中选取（仅 ENABLED）。

### 评审工作台 `/enterprise/reviews`

专家提交评审意见 → `POST /enterprise/reviews/{paperId}`

### 我的投稿 `/portal/submissions`

参会人保存草稿、提交稿件。

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/papers` | 稿件列表 |
| POST | `/enterprise/papers/{id}/assign` | `{ expertId }` 分配专家 |
| PUT | `/enterprise/papers/{id}/finalize` | `{ status: ACCEPTED \| REJECTED \| REVISION }` |
| GET | `/enterprise/reviews` | 专家待审列表 |
| POST | `/enterprise/reviews/{paperId}` | `{ comment, suggest }` |
| GET | `/portal/submissions` | 我的投稿 |
| POST | `/portal/submissions/draft` | 保存草稿 |
| POST | `/portal/submissions/{id}/submit` | 提交稿件 |

---

## 后端实现建议

| 后端 | 说明 |
|------|------|
| assign | `expertId` 须为本租户 `EXPERT` 成员 |
| 状态机 | SUBMITTED → UNDER_REVIEW → REVIEW_DONE → ACCEPTED/REJECTED/REVISION |
