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

活动列表「投稿」统一跳转 `/portal/submissions`（不再按活动分子路由）。

## 设计说明

**草稿与投递分离**

- 草稿不绑定活动（`paper_submission.event_id` 可为 NULL）
- 同一参会人可有多条草稿
- **投递**时再选择「已开放论文投稿」的活动，写入 `event_id` 并变为已投递

**草稿字段**

| 字段 | 必填 | 说明 |
|------|------|------|
| 标题 | 是 | |
| 作者 | 是 | 默认账号昵称，可改 |
| 摘要 | 投递前必填 | 存 `abstract_text` |
| PDF | 投递前必填 | MinIO，`file_key`；草稿阶段可选 |

保存草稿与上传 PDF 独立：点「保存草稿」只存文字；选 PDF 时若尚无稿件 id，会先自动保存草稿再上传。

## 页面行为

### 稿件管理 `/enterprise/papers`

| 操作 | 条件 | API |
|------|------|-----|
| 分配专家 | SUBMITTED / RESUBMITTED | `GET /enterprise/members?role=EXPERT` + `POST /enterprise/papers/{id}/assign` |
| 录用/拒稿/需修改 | REVIEW_DONE | `PUT /enterprise/papers/{id}/finalize` |
| 刷新 | — | `GET /enterprise/papers` |

### 评审工作台 `/enterprise/reviews`

专家提交评审意见 → `POST /enterprise/reviews/{paperId}`

### 我的投稿 `/portal/submissions`

Tab：**草稿** | **已投递**

**草稿 Tab**

| 操作 | 说明 |
|------|------|
| 新建草稿 | 弹窗：标题、作者、摘要、PDF（可选） |
| 编辑 | 修改草稿内容与 PDF |
| 投递 | 弹窗选择活动；校验摘要 + PDF 后提交 |

**已投递 Tab**

| 展示 | 说明 |
|------|------|
| 活动 | `eventTitle` |
| 状态 | 标签 |
| 进度 | 四步：已提交 → 评审中 → 待终审 → 结果 |
| 需修改 | 点击行 → 上传新版 PDF → 重新提交 |

进度与状态映射（前端推导，无历史表）：

| status | 进度节点 | 说明 |
|--------|----------|------|
| SUBMITTED | 1 | 等待分配专家 |
| UNDER_REVIEW / RESUBMITTED | 2 | 专家评审中 |
| REVIEW_DONE | 3 | 等待终审 |
| ACCEPTED / REJECTED / REVISION | 4 | 已有结果 |

---

## API 契约

| Method | Path | 说明 |
|--------|------|------|
| GET | `/enterprise/papers` | 稿件列表 |
| POST | `/enterprise/papers/{id}/assign` | `{ expertId }` 分配专家 |
| PUT | `/enterprise/papers/{id}/finalize` | `{ status: ACCEPTED \| REJECTED \| REVISION }` |
| GET | `/enterprise/reviews` | 专家待审列表 |
| POST | `/enterprise/reviews/{paperId}` | `{ comment, suggest }` |
| GET | `/portal/submissions?scope=` | `draft` \| `submitted`，默认 `draft` |
| POST | `/portal/submissions/draft` | `{ id?, title, author?, abstract? }` 保存/更新草稿 |
| POST | `/portal/submissions/{id}/file` | `multipart/form-data`，字段 `file`，PDF ≤20MB |
| POST | `/portal/submissions/{id}/submit` | 草稿投递：`{ eventId }` 必填；需修改重投：body 可省略 |

### PaperSubmission（参会端）

```ts
{
  id: number
  eventId?: number
  eventTitle?: string
  title: string
  author: string
  abstract?: string
  fileKey?: string
  status: string
  version: number
  submittedAt?: string
}
```

---

## 状态机

```
DRAFT（无 event_id）
  → 投递 + 选择活动 → SUBMITTED
  → UNDER_REVIEW → REVIEW_DONE
  → ACCEPTED | REJECTED | REVISION
REVISION → 上传新版 → RESUBMITTED → UNDER_REVIEW（循环）
```

---

## 后端实现

| 后端 | 说明 |
|------|------|
| PaperService | 草稿、上传、投递、分配、评审、终审 |
| FileStorageService | MinIO 存储 PDF |
| SQL V9 | `abstract_text` |
| SQL V10 | `event_id` 可 NULL（草稿） |
