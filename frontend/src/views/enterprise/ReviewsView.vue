<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewTasks, submitReview } from '@/api/enterprise'

interface ReviewTask {
  paperId: number
  title: string
  author: string
  deadline: string
}

const loading = ref(false)
const submitting = ref(false)
const list = ref<ReviewTask[]>([])
const dialogVisible = ref(false)
const current = ref<ReviewTask | null>(null)
const form = ref({ comment: '', suggest: 'accept' })

async function load() {
  loading.value = true
  try {
    list.value = await getReviewTasks()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openReview(row: ReviewTask) {
  current.value = row
  form.value = { comment: '', suggest: 'accept' }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!current.value || !form.value.comment) return ElMessage.warning('请填写评审意见')
  submitting.value = true
  try {
    await submitReview(current.value.paperId, form.value)
    list.value = list.value.filter((r) => r.paperId !== current.value!.paperId)
    ElMessage.success('评审已提交')
    dialogVisible.value = false
  } catch {
    return
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>评审工作台</h1>
      <p>仅显示分配给您的待审稿件</p>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无待审稿件" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="deadline" label="截止日期" width="120" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row)">提交评审</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="提交评审" width="520px">
      <el-form label-width="80px">
        <el-form-item label="评审意见" required>
          <el-input v-model="form.comment" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="建议">
          <el-radio-group v-model="form.suggest">
            <el-radio value="accept">录用</el-radio>
            <el-radio value="reject">拒稿</el-radio>
            <el-radio value="revision">需修改</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交评审</el-button>
      </template>
    </el-dialog>
  </div>
</template>
