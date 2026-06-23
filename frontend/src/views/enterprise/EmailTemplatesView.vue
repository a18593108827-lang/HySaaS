<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getEmailTemplates, updateEmailTemplate, getEmailLogs, getEvents } from '@/api/enterprise'

interface Template {
  id: number
  code: string
  name: string
  subject: string
  content: string
  eventId?: number
}

const loading = ref(false)
const logLoading = ref(false)
const activeTab = ref('templates')
const list = ref<Template[]>([])
const logs = ref<{ id: number; code: string; recipient: string; subject: string; status: string; errorMsg?: string; createdAt: string }[]>([])
const events = ref<{ id: number; title: string }[]>([])
const eventId = ref<number | undefined>()
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

async function loadEvents() {
  try {
    const res = await getEvents({ page: 0, size: 100 })
    events.value = res.records.map((e) => ({ id: e.id, title: e.title }))
  } catch {
    events.value = []
  }
}

async function load() {
  loading.value = true
  try {
    list.value = await getEmailTemplates({ eventId: eventId.value })
  } catch {
    list.value = []
    ElMessage.error('加载邮件模板失败')
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const res = await getEmailLogs({ page: 0, size: 20 })
    logs.value = res.records
  } catch {
    logs.value = []
    ElMessage.error('加载发信记录失败')
  } finally {
    logLoading.value = false
  }
}

function openEdit(row: Template) {
  editing.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!editing.value) return
  try {
    await updateEmailTemplate(editing.value.id, {
      subject: editing.value.subject,
      content: editing.value.content,
    })
    const idx = list.value.findIndex((t) => t.id === editing.value!.id)
    if (idx >= 0) list.value[idx] = { ...editing.value }
    ElMessage.success('模板已保存')
    dialogVisible.value = false
    editing.value = null
  } catch {
    return
  }
}

watch(eventId, load)

onMounted(() => {
  loadEvents()
  load()
  loadLogs()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h1>邮件模板</h1>
      <p>支持 <code v-pre>{{name}}</code>、<code v-pre>{{eventName}}</code>、<code v-pre>{{status}}</code> 等变量</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="模板" name="templates">
        <div style="margin-bottom: 12px">
          <el-select v-model="eventId" placeholder="租户默认模板" clearable style="width: 260px" @clear="eventId = undefined">
            <el-option v-for="e in events" :key="e.id" :label="e.title" :value="e.id" />
          </el-select>
        </div>
        <el-table v-loading="loading" :data="list" border stripe>
          <el-table-column prop="code" label="编码" width="160" />
          <el-table-column prop="name" label="名称" width="110" />
          <el-table-column prop="subject" label="主题" width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="正文" min-width="240" show-overflow-tooltip />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="发信记录" name="logs">
        <el-table v-loading="logLoading" :data="logs" border stripe>
          <el-table-column prop="createdAt" label="时间" width="170" />
          <el-table-column prop="code" label="编码" width="140" />
          <el-table-column prop="recipient" label="收件人" min-width="160" />
          <el-table-column prop="status" label="状态" width="80" />
          <el-table-column prop="errorMsg" label="错误" min-width="180" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="editing?.name" width="560px">
      <el-form v-if="editing" label-width="60px">
        <el-form-item label="主题">
          <el-input v-model="editing.subject" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="editing.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
