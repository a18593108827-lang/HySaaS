<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMySubmissions, saveDraft, submitPaper } from '@/api/portal'
import type { PaperSubmission } from '@/types'

const loading = ref(false)
const list = ref<PaperSubmission[]>([])
const dialogVisible = ref(false)
const form = ref({ title: '', abstract: '' })

const statusLabel: Record<string, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  UNDER_REVIEW: '评审中',
  ACCEPTED: '已录用',
  REJECTED: '已拒稿',
  REVISION: '需修改',
}

async function load() {
  loading.value = true
  try {
    list.value = await getMySubmissions()
  } catch {
    list.value = [
      { id: 1, title: '我的研究论文', author: '我', status: 'DRAFT', version: 1, submittedAt: '' },
    ]
  } finally {
    loading.value = false
  }
}

async function handleSaveDraft() {
  if (!form.value.title) return ElMessage.warning('请填写标题')
  try {
    await saveDraft(form.value)
    ElMessage.success('草稿已保存')
    dialogVisible.value = false
    form.value = { title: '', abstract: '' }
    await load()
  } catch {
    return
  }
}

async function handleSubmit(row: PaperSubmission) {
  try {
    await submitPaper(row.id)
    row.status = 'SUBMITTED'
    ElMessage.success('稿件已提交')
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>我的投稿</h1>
      <p>保存草稿、提交稿件、查看评审状态</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="dialogVisible = true">新建投稿</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">{{ statusLabel[row.status] || row.status }}</template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'DRAFT'" link type="primary" @click="handleSubmit(row)">提交</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建投稿" width="520px">
      <el-form label-width="60px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.abstract" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="文件">
          <el-upload action="#" :auto-upload="false">
            <el-button>上传 PDF</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>
