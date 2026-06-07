<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEvent, deleteEvent, getEvents, publishEvent, updateEvent } from '@/api/enterprise'
import { useEventStore } from '@/stores/event'
import CheckinQrcodeDialog from '@/components/CheckinQrcodeDialog.vue'
import InviteAttendeesDialog from '@/components/InviteAttendeesDialog.vue'
import type { EventItem } from '@/types'

const router = useRouter()
const eventStore = useEventStore()
const loading = ref(false)
const list = ref<EventItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationEnabled: false,
  paperEnabled: false,
  hotelEnabled: false,
})

const defaultForm = () => ({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationEnabled: false,
  paperEnabled: false,
  hotelEnabled: false,
})
const qrcodeVisible = ref(false)
const qrcodeEvent = ref<EventItem | null>(null)
const inviteVisible = ref(false)
const inviteEvent = ref<EventItem | null>(null)

const statusMap: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  REGISTRATION_OPEN: '报名中',
  REGISTRATION_CLOSED: '报名截止',
}

async function load() {
  loading.value = true
  try {
    const res = await getEvents()
    list.value = res.records
  } catch {
    list.value = [
      { id: 1, title: '2026 医学年会', location: '上海', startTime: '2026-09-15', endTime: '2026-09-17', status: 'REGISTRATION_OPEN', registrationEnabled: true, paperEnabled: true, hotelEnabled: true },
      { id: 2, title: '创新技术论坛', location: '深圳', startTime: '2026-10-20', endTime: '2026-10-21', status: 'DRAFT', registrationEnabled: false, paperEnabled: false, hotelEnabled: false },
    ]
  }
  eventStore.setList(list.value)
  loading.value = false
}

function goRegistrations(row: EventItem) {
  eventStore.setActiveEventId(row.id)
  eventStore.setCurrent(row)
  router.push(`/enterprise/events/${row.id}/registrations`)
}

function goCheckin(row: EventItem) {
  eventStore.setActiveEventId(row.id)
  eventStore.setCurrent(row)
  router.push(`/enterprise/events/${row.id}/checkin`)
}

function openQrcode(row: EventItem) {
  qrcodeEvent.value = row
  qrcodeVisible.value = true
}

function openInvite(row: EventItem) {
  inviteEvent.value = row
  inviteVisible.value = true
}

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

function openEdit(row: EventItem) {
  editingId.value = row.id
  form.value = {
    title: row.title,
    location: row.location,
    startTime: row.startTime,
    endTime: row.endTime,
    registrationEnabled: row.registrationEnabled,
    paperEnabled: row.paperEnabled,
    hotelEnabled: row.hotelEnabled,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.title) return ElMessage.warning('请填写活动名称')
  if (editingId.value) {
    try {
      await updateEvent(editingId.value, form.value)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = list.value.find((e) => e.id === editingId.value)
    if (row) Object.assign(row, form.value)
  } else {
    try {
      await createEvent(form.value)
      ElMessage.success('活动已创建')
    } catch {
      list.value.unshift({
        id: Date.now(),
        ...form.value,
        status: 'DRAFT',
      } as EventItem)
      ElMessage.success('演示：活动已创建')
    }
  }
  dialogVisible.value = false
  editingId.value = null
  form.value = defaultForm()
}

async function handleDelete(row: EventItem) {
  await ElMessageBox.confirm(`删除活动「${row.title}」？此操作不可恢复`, '删除确认', { type: 'warning' })
  try {
    await deleteEvent(row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  list.value = list.value.filter((e) => e.id !== row.id)
  if (eventStore.activeEventId === row.id) {
    eventStore.setActiveEventId(list.value[0]?.id ?? null)
  }
}

async function handlePublish(row: EventItem) {
  await ElMessageBox.confirm(`发布活动「${row.title}」？`, '发布确认')
  try {
    await publishEvent(row.id)
    row.status = 'PUBLISHED'
    ElMessage.success('已发布')
  } catch {
    row.status = 'PUBLISHED'
    ElMessage.success('演示：已发布')
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>活动管理</h1>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新建活动</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="title" label="活动名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="location" label="地点" width="90" />
      <el-table-column label="时间" width="190">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ statusMap[row.status] }}</template>
      </el-table-column>
      <el-table-column label="功能" width="150">
        <template #default="{ row }">
          <el-tag v-if="row.registrationEnabled" size="small" style="margin-right: 4px">报名</el-tag>
          <el-tag v-if="row.paperEnabled" size="small" type="success" style="margin-right: 4px">论文</el-tag>
          <el-tag v-if="row.hotelEnabled" size="small" type="warning">酒店</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="460" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goRegistrations(row)">报名审核</el-button>
          <el-button link type="primary" @click="goCheckin(row)">签到管理</el-button>
          <el-button link type="primary" @click="openQrcode(row)">获取二维码</el-button>
          <el-button link type="primary" @click="openInvite(row)">邀请参会</el-button>
          <el-button
            v-if="row.status === 'DRAFT'"
            link
            type="primary"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑活动' : '新建活动'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="开始">
          <el-date-picker v-model="form.startTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker v-model="form.endTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开放功能">
          <div class="switch-group">
            <el-switch v-model="form.registrationEnabled" active-text="报名" />
            <el-switch v-model="form.paperEnabled" active-text="论文" />
            <el-switch v-model="form.hotelEnabled" active-text="酒店" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <CheckinQrcodeDialog
      v-if="qrcodeEvent"
      v-model="qrcodeVisible"
      :event-id="qrcodeEvent.id"
      :event-title="qrcodeEvent.title"
    />

    <InviteAttendeesDialog
      v-if="inviteEvent"
      v-model="inviteVisible"
      :event-id="inviteEvent.id"
      :event-title="inviteEvent.title"
    />
  </div>
</template>

<style scoped>
.switch-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
