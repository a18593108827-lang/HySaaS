<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { getMySubmissions, getPortalEvents, saveDraft, submitPaper, uploadPaperFile } from '@/api/portal'
import { useAuthStore } from '@/stores/auth'
import PortalBackBar from '@/components/PortalBackBar.vue'
import type { EventItem, PaperSubmission } from '@/types'

const auth = useAuthStore()

const activeTab = ref<'draft' | 'submitted'>('draft')
const loading = ref(false)
const list = ref<PaperSubmission[]>([])
const dialogVisible = ref(false)
const submitDialogVisible = ref(false)
const saving = ref(false)
const uploading = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const submitTarget = ref<PaperSubmission | null>(null)
const submitEventId = ref<string>('')
const paperEvents = ref<EventItem[]>([])
const form = ref({ title: '', author: '', abstract: '' })
const fileKey = ref('')

const statusLabel: Record<string, string> = {
  SUBMITTED: '已提交',
  UNDER_REVIEW: '评审中',
  REVIEW_DONE: '评审完成',
  ACCEPTED: '已录用',
  REJECTED: '已拒稿',
  REVISION: '需修改',
  RESUBMITTED: '已重投',
}

const progressSteps = ['已提交', '评审中', '待终审', '结果']

function paperProgress(status: string) {
  switch (status) {
    case 'SUBMITTED':
      return { active: 0, hint: '等待分配专家', result: '' }
    case 'UNDER_REVIEW':
    case 'RESUBMITTED':
      return { active: 1, hint: '专家评审中', result: '' }
    case 'REVIEW_DONE':
      return { active: 2, hint: '等待终审', result: '' }
    case 'ACCEPTED':
      return { active: 3, hint: '已录用', result: 'success' }
    case 'REJECTED':
      return { active: 3, hint: '已拒稿', result: 'danger' }
    case 'REVISION':
      return { active: 3, hint: '需修改后重投', result: 'warning' }
    default:
      return { active: 0, hint: '', result: '' }
  }
}

function fileName(key?: string) {
  if (!key) return ''
  return key.split('/').pop() || key
}

function resetForm() {
  editingId.value = null
  form.value = { title: '', author: auth.user?.nickname || '', abstract: '' }
  fileKey.value = ''
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: PaperSubmission) {
  editingId.value = row.id
  form.value = {
    title: row.title,
    author: row.author || auth.user?.nickname || '',
    abstract: row.abstract || '',
  }
  fileKey.value = row.fileKey || ''
  dialogVisible.value = true
}

async function loadPaperEvents() {
  try {
    const events = await getPortalEvents()
    paperEvents.value = events.filter((e) => e.paperEnabled)
  } catch {
    paperEvents.value = []
  }
}

async function load() {
  loading.value = true
  try {
    list.value = await getMySubmissions(activeTab.value)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

watch(activeTab, load)

async function handleSaveDraft() {
  if (!form.value.title.trim()) return ElMessage.warning('请填写标题')
  if (!form.value.author.trim()) return ElMessage.warning('请填写作者')
  saving.value = true
  try {
    const saved = await saveDraft({
      id: editingId.value ?? undefined,
      title: form.value.title.trim(),
      author: form.value.author.trim(),
      abstract: form.value.abstract.trim(),
    })
    editingId.value = saved.id
    fileKey.value = saved.fileKey || fileKey.value
    ElMessage.success('草稿已保存')
    await load()
  } catch {
    return
  } finally {
    saving.value = false
  }
}

async function ensureDraftId() {
  if (editingId.value) return editingId.value
  if (!form.value.title.trim()) {
    ElMessage.warning('请先填写标题')
    throw new Error('no title')
  }
  if (!form.value.author.trim()) {
    ElMessage.warning('请先填写作者')
    throw new Error('no author')
  }
  const saved = await saveDraft({
    title: form.value.title.trim(),
    author: form.value.author.trim(),
    abstract: form.value.abstract.trim(),
  })
  editingId.value = saved.id
  fileKey.value = saved.fileKey || fileKey.value
  return saved.id
}

async function handleUpload(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const id = await ensureDraftId()
    const saved = await uploadPaperFile(id, options.file as File)
    fileKey.value = saved.fileKey || ''
    ElMessage.success('PDF 已上传')
    options.onSuccess?.(saved as never)
    await load()
  } catch (e) {
    options.onError?.(e as never)
  } finally {
    uploading.value = false
  }
}

function openSubmitDialog(row: PaperSubmission) {
  if (!row.abstract?.trim()) return ElMessage.warning('请先填写摘要')
  if (!row.fileKey) return ElMessage.warning('投递前请先上传 PDF')
  submitTarget.value = row
  submitEventId.value = ''
  submitDialogVisible.value = true
  loadPaperEvents()
}

async function confirmSubmit() {
  if (!submitTarget.value) return
  if (!submitEventId.value) return ElMessage.warning('请选择投稿活动')
  submitting.value = true
  try {
    await submitPaper(submitTarget.value.id, submitEventId.value)
    ElMessage.success('稿件已投递')
    submitDialogVisible.value = false
    activeTab.value = 'submitted'
  } catch {
    return
  } finally {
    submitting.value = false
  }
}

async function confirmRevisionSubmit() {
  if (!editingId.value) return
  submitting.value = true
  try {
    await submitPaper(editingId.value)
    ElMessage.success('稿件已重新提交')
    dialogVisible.value = false
    await load()
  } catch {
    return
  } finally {
    submitting.value = false
  }
}

function openRevisionUpload(row: PaperSubmission) {
  editingId.value = row.id
  form.value = {
    title: row.title,
    author: row.author,
    abstract: row.abstract || '',
  }
  fileKey.value = row.fileKey || ''
  dialogVisible.value = true
}

function onSubmittedRowClick(row: PaperSubmission) {
  if (row.status === 'REVISION') openRevisionUpload(row)
}

onMounted(load)
</script>

<template>
  <div>
    <PortalBackBar />
    <div class="page-header">
      <h1>我的投稿</h1>
      <p>草稿独立保存，投递时再选择活动</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="草稿" name="draft">
        <div class="toolbar">
          <el-button type="primary" @click="openCreate">新建草稿</el-button>
        </div>
        <el-table v-loading="loading" :data="list" border stripe>
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="author" label="作者" width="100" />
          <el-table-column label="PDF" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.fileKey" size="small" type="success">已传</el-tag>
              <el-tag v-else size="small" type="info">未传</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="openSubmitDialog(row)">投递</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !list.length" description="暂无草稿" />
      </el-tab-pane>

      <el-tab-pane label="已投递" name="submitted">
        <p v-if="list.some((r) => r.status === 'REVISION')" class="revision-tip">「需修改」稿件点击行可上传新版并重新提交</p>
        <el-table
          v-loading="loading"
          :data="list"
          border
          stripe
          :row-class-name="({ row }: { row: PaperSubmission }) => row.status === 'REVISION' ? 'row-revision' : ''"
          @row-click="onSubmittedRowClick"
        >
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="eventTitle" label="活动" min-width="140" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="100" />
          <el-table-column prop="status" label="状态" width="96">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="row.status === 'ACCEPTED' ? 'success' : row.status === 'REJECTED' ? 'danger' : row.status === 'REVISION' ? 'warning' : 'info'"
              >
                {{ statusLabel[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" min-width="220">
            <template #default="{ row }">
              <div class="progress-cell">
                <div class="progress-track">
                  <template v-for="(step, i) in progressSteps" :key="step">
                    <span
                      class="progress-dot"
                      :class="[
                        i < paperProgress(row.status).active ? 'done' : '',
                        i === paperProgress(row.status).active && !paperProgress(row.status).result ? 'current' : '',
                        i === paperProgress(row.status).active && paperProgress(row.status).result === 'success' ? 'final-success' : '',
                        i === paperProgress(row.status).active && paperProgress(row.status).result === 'danger' ? 'final-danger' : '',
                        i === paperProgress(row.status).active && paperProgress(row.status).result === 'warning' ? 'final-warning' : '',
                      ]"
                      :title="step"
                    />
                    <span v-if="i < progressSteps.length - 1" class="progress-line" :class="{ done: i < paperProgress(row.status).active }" />
                  </template>
                </div>
                <div class="progress-labels">
                  <span v-for="(step, i) in progressSteps" :key="step" :class="{ active: i === paperProgress(row.status).active }">{{ step }}</span>
                </div>
                <p class="progress-hint">{{ paperProgress(row.status).hint }}</p>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" width="70" />
          <el-table-column prop="submittedAt" label="投递时间" width="170" />
        </el-table>
        <el-empty v-if="!loading && !list.length" description="暂无已投递稿件" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="dialogVisible"
      :title="activeTab === 'submitted' && editingId ? '上传新版 PDF' : (editingId ? '编辑草稿' : '新建草稿')"
      width="560px"
      @closed="resetForm"
    >
      <el-form label-width="72px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" :disabled="activeTab === 'submitted'" />
        </el-form-item>
        <el-form-item label="作者" required>
          <el-input v-model="form.author" :disabled="activeTab === 'submitted'" />
        </el-form-item>
        <el-form-item label="摘要" required>
          <el-input v-model="form.abstract" type="textarea" :rows="4" :disabled="activeTab === 'submitted'" placeholder="200–500 字" />
        </el-form-item>
        <el-form-item label="PDF">
          <div class="upload-wrap">
            <el-upload :show-file-list="false" accept=".pdf,application/pdf" :http-request="handleUpload">
              <el-button :loading="uploading">选择 PDF</el-button>
            </el-upload>
            <span v-if="fileKey" class="file-name">{{ fileName(fileKey) }}</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="activeTab === 'draft'" type="primary" :loading="saving" @click="handleSaveDraft">保存草稿</el-button>
        <el-button
          v-if="activeTab === 'submitted' && editingId"
          type="primary"
          :loading="submitting"
          @click="confirmRevisionSubmit"
        >重新提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submitDialogVisible" title="投递稿件" width="440px">
      <p class="submit-tip">将草稿投递至以下活动，投递后进入「已投递」列表</p>
      <el-form label-width="80px">
        <el-form-item label="稿件">
          <span>{{ submitTarget?.title }}</span>
        </el-form-item>
        <el-form-item label="投稿活动" required>
          <el-select v-model="submitEventId" placeholder="选择开放论文投稿的活动" style="width: 100%">
            <el-option v-for="e in paperEvents" :key="e.id" :label="e.title" :value="String(e.id)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmSubmit">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { margin-bottom: 1rem; }
.upload-wrap { display: flex; flex-direction: column; gap: 0.5rem; align-items: flex-start; }
.file-name { font-size: 0.875rem; color: var(--muted); word-break: break-all; }
.submit-tip { margin: 0 0 1rem; font-size: 0.875rem; color: var(--muted); }
.revision-tip { margin: 0 0 0.75rem; font-size: 0.875rem; color: var(--muted); }
:deep(.row-revision) { cursor: pointer; }
:deep(.row-revision:hover td) { background: var(--el-fill-color-light) !important; }
.progress-cell { padding: 4px 0; }
.progress-track { display: flex; align-items: center; }
.progress-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
  background: var(--border); transition: background 0.2s;
}
.progress-dot.done, .progress-dot.current { background: var(--primary); }
.progress-dot.final-success { background: var(--el-color-success); }
.progress-dot.final-danger { background: var(--el-color-danger); }
.progress-dot.final-warning { background: var(--el-color-warning); }
.progress-line { flex: 1; height: 2px; margin: 0 2px; background: var(--border); }
.progress-line.done { background: var(--primary); }
.progress-labels {
  display: flex; justify-content: space-between; margin-top: 4px;
  font-size: 0.6875rem; color: var(--muted);
}
.progress-labels .active { color: var(--primary); font-weight: 600; }
.progress-hint { margin: 4px 0 0; font-size: 0.75rem; color: var(--muted); }
</style>
