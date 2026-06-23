<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEvent, deleteEvent, getEvents, publishEvent, updateEvent } from '@/api/enterprise'
import { useEventStore } from '@/stores/event'
import CheckinQrcodeDialog from '@/components/CheckinQrcodeDialog.vue'
import InviteAttendeesDialog from '@/components/InviteAttendeesDialog.vue'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import type { EventItem } from '@/types'

const router = useRouter()
const eventStore = useEventStore()
const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const submitting = ref(false)
const list = ref<EventItem[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | string | null>(null)
const form = ref({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationEnabled: false,
  paperEnabled: false,
  hotelEnabled: false,
  registrationFee: 0,
})

const defaultForm = () => ({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationEnabled: false,
  paperEnabled: false,
  hotelEnabled: false,
  registrationFee: 0,
})
const qrcodeVisible = ref(false)
const qrcodeEvent = ref<EventItem | null>(null)
const inviteVisible = ref(false)
const inviteEvent = ref<EventItem | null>(null)
const endTimeError = ref('')

const statusMap: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  REGISTRATION_OPEN: '报名中',
  REGISTRATION_CLOSED: '报名截止',
}

async function load() {
  loading.value = true
  try {
    const res = await getEvents({ page: page.value, size: size.value })
    list.value = res.records
    total.value = res.total
    eventStore.setList(list.value)
  } catch {
    list.value = []
    total.value = 0
    eventStore.setList([])
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
  endTimeError.value = ''
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
    registrationFee: row.registrationFee ?? 0,
  }
  endTimeError.value = ''
  dialogVisible.value = true
}

function validateEventDates() {
  endTimeError.value = ''
  if (form.value.startTime && form.value.endTime && form.value.endTime < form.value.startTime) {
    endTimeError.value = '结束时间不能早于开始时间'
    return false
  }
  return true
}

async function handleSubmit() {
  if (!form.value.title) return ElMessage.warning('请填写活动名称')
  if (!validateEventDates()) return
  submitting.value = true
  try {
    if (editingId.value) {
      await updateEvent(editingId.value, form.value)
      ElMessage.success('已保存')
      const row = list.value.find((e) => e.id === editingId.value)
      if (row) Object.assign(row, form.value)
    } else {
      await createEvent(form.value)
      ElMessage.success('活动已创建')
      await load()
    }
    dialogVisible.value = false
    editingId.value = null
    form.value = defaultForm()
  } catch {
    return
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: EventItem) {
  await ElMessageBox.confirm(`删除活动「${row.title}」？此操作不可恢复`, '删除确认', { type: 'warning' })
  try {
    await deleteEvent(row.id)
    ElMessage.success('已删除')
    list.value = list.value.filter((e) => e.id !== row.id)
    if (String(eventStore.activeEventId) === String(row.id)) {
      eventStore.setActiveEventId(list.value[0]?.id ?? null)
    }
  } catch {
    return
  }
}

async function handlePublish(row: EventItem) {
  await ElMessageBox.confirm(`发布活动「${row.title}」？`, '发布确认')
  try {
    await publishEvent(row.id)
    row.status = 'PUBLISHED'
    ElMessage.success('已发布')
  } catch {
    return
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
    <el-empty v-if="!loading && !list.length" description="暂无活动" />
    <el-table v-else v-loading="loading" :data="list" border stripe style="width: 100%">
      <el-table-column prop="title" label="活动名称" width="150" show-overflow-tooltip />
      <el-table-column prop="location" label="地点" width="100" show-overflow-tooltip />
      <el-table-column label="时间" width="190">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ statusMap[row.status] }}</template>
      </el-table-column>
      <el-table-column label="功能" width="130">
        <template #default="{ row }">
          <div class="feature-tags">
            <el-tag v-if="row.registrationEnabled" size="small">报名</el-tag>
            <el-tag v-if="row.paperEnabled" size="small" type="success">论文</el-tag>
            <el-tag v-if="row.hotelEnabled" size="small" type="warning">酒店</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" size="small" @click="goRegistrations(row)">报名</el-button>
            <el-button link type="primary" size="small" @click="goCheckin(row)">签到</el-button>
            <el-button link type="primary" size="small" @click="openQrcode(row)">二维码</el-button>
            <el-button link type="primary" size="small" @click="openInvite(row)">邀请</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="primary" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />

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
        <el-form-item label="结束" :error="endTimeError">
          <el-date-picker v-model="form.endTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.registrationEnabled" label="报名费">
          <el-input-number v-model="form.registrationFee" :min="0" :precision="2" :step="10" style="width: 100%" />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
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

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0 4px;
}
</style>
