<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assignReviewer, finalizePaper, getMembers, getPapers } from '@/api/enterprise'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import type { EnterpriseMember, PaperSubmission } from '@/types'

const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const list = ref<PaperSubmission[]>([])
const assignDialog = ref(false)
const assignSubmitting = ref(false)
const expertLoading = ref(false)
const currentPaper = ref<PaperSubmission | null>(null)
const expertId = ref<number>()
const experts = ref<EnterpriseMember[]>([])

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  UNDER_REVIEW: '评审中',
  REVIEW_DONE: '评审完成',
  ACCEPTED: '已录用',
  REJECTED: '已拒稿',
  REVISION: '需修改',
  RESUBMITTED: '已重投',
}

async function loadExperts() {
  expertLoading.value = true
  try {
    const res = await getMembers({ role: 'EXPERT' })
    experts.value = res.records.filter((m) => m.status === 'ENABLED')
  } catch {
    experts.value = []
  } finally {
    expertLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res = await getPapers({ page: page.value, size: size.value })
    list.value = res.records
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function onSizeChange(s: number) {
  size.value = s
  resetPage()
  load()
}

async function openAssign(row: PaperSubmission) {
  currentPaper.value = row
  expertId.value = undefined
  assignDialog.value = true
  await loadExperts()
}

async function handleAssign() {
  if (!currentPaper.value) return
  if (!expertId.value) return ElMessage.warning('请选择专家')
  assignSubmitting.value = true
  try {
    await assignReviewer(currentPaper.value.id, expertId.value)
    currentPaper.value.status = 'UNDER_REVIEW'
    ElMessage.success('已分配专家')
    assignDialog.value = false
  } catch {
    return
  } finally {
    assignSubmitting.value = false
  }
}

async function handleFinalize(row: PaperSubmission, status: 'ACCEPTED' | 'REJECTED' | 'REVISION') {
  const labels = { ACCEPTED: '录用', REJECTED: '拒稿', REVISION: '需修改' }
  await ElMessageBox.confirm(`确认${labels[status]}「${row.title}」？`, '终审确认')
  try {
    await finalizePaper(row.id, status)
    row.status = status
    ElMessage.success(`已${labels[status]}`)
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>稿件管理</h1>
      <p>分配专家、终审录用/拒稿/需修改</p>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无稿件" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">{{ statusLabel[row.status] || row.status }}</template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column prop="submittedAt" label="提交时间" width="120" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'SUBMITTED' || row.status === 'RESUBMITTED'" link type="primary" @click="openAssign(row)">分配专家</el-button>
          <template v-if="row.status === 'REVIEW_DONE'">
            <el-button link type="success" @click="handleFinalize(row, 'ACCEPTED')">录用</el-button>
            <el-button link type="danger" @click="handleFinalize(row, 'REJECTED')">拒稿</el-button>
            <el-button link @click="handleFinalize(row, 'REVISION')">需修改</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />

    <el-dialog v-model="assignDialog" title="分配评审专家" width="400px">
      <el-select v-model="expertId" placeholder="选择专家" :loading="expertLoading" style="width: 100%">
        <el-option v-for="e in experts" :key="e.id" :label="e.nickname" :value="e.id">
          <span>{{ e.nickname }}</span>
          <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ e.username }}</span>
        </el-option>
      </el-select>
      <p v-if="!expertLoading && !experts.length" class="empty-hint">
        暂无专家成员，请先在「成员管理」中添加 EXPERT 角色账号
      </p>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitting" :disabled="!experts.length" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.empty-hint {
  margin: 0.75rem 0 0;
  font-size: 0.8125rem;
  color: var(--muted);
}
</style>
