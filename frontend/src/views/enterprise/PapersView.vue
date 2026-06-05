<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assignReviewer, finalizePaper, getPapers } from '@/api/enterprise'
import type { PaperSubmission } from '@/types'

const loading = ref(false)
const list = ref<PaperSubmission[]>([])
const assignDialog = ref(false)
const currentPaper = ref<PaperSubmission | null>(null)
const expertId = ref<number>()

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

async function load() {
  loading.value = true
  try {
    const res = await getPapers()
    list.value = res.records
  } catch {
    list.value = [
      { id: 1, title: '深度学习在影像诊断中的应用', author: '张三', status: 'SUBMITTED', version: 1, submittedAt: '2026-06-01' },
      { id: 2, title: '新型材料力学性能研究', author: '李四', status: 'UNDER_REVIEW', version: 1, submittedAt: '2026-05-28' },
    ]
  } finally {
    loading.value = false
  }
}

function openAssign(row: PaperSubmission) {
  currentPaper.value = row
  expertId.value = undefined
  assignDialog.value = true
}

async function handleAssign() {
  if (!currentPaper.value || !expertId.value) return
  try {
    await assignReviewer(currentPaper.value.id, expertId.value)
    currentPaper.value.status = 'UNDER_REVIEW'
    ElMessage.success('已分配专家')
  } catch {
    currentPaper.value!.status = 'UNDER_REVIEW'
    ElMessage.success('演示：已分配专家')
  }
  assignDialog.value = false
}

async function handleFinalize(row: PaperSubmission, status: 'ACCEPTED' | 'REJECTED' | 'REVISION') {
  const labels = { ACCEPTED: '录用', REJECTED: '拒稿', REVISION: '需修改' }
  await ElMessageBox.confirm(`确认${labels[status]}「${row.title}」？`, '终审确认')
  try {
    await finalizePaper(row.id, status)
    row.status = status
    ElMessage.success(`已${labels[status]}`)
  } catch {
    row.status = status
    ElMessage.success(`演示：已${labels[status]}`)
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
    <el-table v-loading="loading" :data="list" border stripe>
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

    <el-dialog v-model="assignDialog" title="分配评审专家" width="400px">
      <el-select v-model="expertId" placeholder="选择专家" style="width: 100%">
        <el-option label="王教授" :value="101" />
        <el-option label="刘研究员" :value="102" />
      </el-select>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>
