<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEmailTemplates, updateEmailTemplate } from '@/api/enterprise'

interface Template {
  id: number
  code: string
  name: string
  content: string
}

const loading = ref(false)
const list = ref<Template[]>([])
const dialogVisible = ref(false)
const editing = ref<Template | null>(null)

const codeNames: Record<string, string> = {
  PAPER_SUBMITTED: '投稿提交',
  PAPER_UNDER_REVIEW: '进入评审',
  PAPER_ACCEPTED: '录用通知',
  PAPER_REJECTED: '拒稿通知',
  PAPER_REVISION: '需修改通知',
  REG_APPROVED: '报名通过',
  REG_REJECTED: '报名拒绝',
  PAY_SUCCESS: '支付成功',
  INVOICE_READY: '发票就绪',
}

async function load() {
  loading.value = true
  try {
    list.value = await getEmailTemplates()
  } catch {
    list.value = Object.entries(codeNames).map(([code, name], i) => ({
      id: i + 1,
      code,
      name,
      content: `尊敬的 {{name}}，关于 {{eventName}}：{{status}}`,
    }))
  } finally {
    loading.value = false
  }
}

function openEdit(row: Template) {
  editing.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!editing.value) return
  try {
    await updateEmailTemplate(editing.value.id, editing.value.content)
    const idx = list.value.findIndex((t) => t.id === editing.value!.id)
    if (idx >= 0) list.value[idx] = { ...editing.value }
    ElMessage.success('模板已保存')
    dialogVisible.value = false
    editing.value = null
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>邮件模板</h1>
      <p>支持 <code v-pre>{{name}}</code>、<code v-pre>{{eventName}}</code>、<code v-pre>{{status}}</code> 变量替换</p>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="code" label="编码" width="180" />
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="content" label="模板内容" min-width="280" show-overflow-tooltip />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing?.name" width="560px">
      <el-input v-if="editing" v-model="editing.content" type="textarea" :rows="8" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存模板</el-button>
      </template>
    </el-dialog>
  </div>
</template>
