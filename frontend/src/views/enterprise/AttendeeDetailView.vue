<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAttendee, getAttendee, updateAttendee } from '@/api/enterprise'
import { validateEmail, validatePassword, validatePhone } from '@/utils/account'
import type { EnterpriseAttendee } from '@/types'

const route = useRoute()
const router = useRouter()
const attendeeId = computed(() => String(route.params.id))
const loading = ref(false)
const attendee = ref<EnterpriseAttendee | null>(null)
const dialogVisible = ref(false)
const form = ref({
  email: '',
  phone: '',
  nickname: '',
  password: '',
  status: 'ENABLED' as EnterpriseAttendee['status'],
})
const emailError = ref('')
const phoneError = ref('')
const passwordErrorMsg = ref('')

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

async function load() {
  loading.value = true
  try {
    attendee.value = await getAttendee(attendeeId.value)
  } catch {
    attendee.value = null
    ElMessage.error('加载参会账号详情失败')
  } finally {
    loading.value = false
  }
}

function openEdit() {
  if (!attendee.value) return
  form.value = {
    email: attendee.value.email || attendee.value.username,
    phone: attendee.value.phone || '',
    nickname: attendee.value.nickname,
    password: '',
    status: attendee.value.status,
  }
  emailError.value = ''
  phoneError.value = ''
  passwordErrorMsg.value = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!attendee.value || !form.value.nickname) return ElMessage.warning('请填写昵称')
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (emailError.value || phoneError.value) return
  if (form.value.password) {
    passwordErrorMsg.value = validatePassword(form.value.password, false)
    if (passwordErrorMsg.value) return
  }
  const payload = {
    email: form.value.email.trim().toLowerCase(),
    phone: form.value.phone.trim(),
    nickname: form.value.nickname,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  try {
    attendee.value = await updateAttendee(attendee.value.id, payload)
    ElMessage.success('已保存')
    dialogVisible.value = false
  } catch {
    return
  }
}

async function handleDelete() {
  if (!attendee.value) return
  await ElMessageBox.confirm(`删除参会账号「${attendee.value.nickname}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteAttendee(attendee.value.id)
    ElMessage.success('已删除')
    router.push('/enterprise/attendees')
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/enterprise/attendees')">← 返回列表</el-button>
    </div>
    <template v-if="attendee">
      <div class="page-header">
        <h1>{{ attendee.nickname }}</h1>
        <el-tag :type="statusMap[attendee.status]?.type">{{ statusMap[attendee.status]?.label }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="邮箱">{{ attendee.email || attendee.username }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ attendee.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ attendee.nickname }}</el-descriptions-item>
        <el-descriptions-item label="类型">参会（ATTENDEE）</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ attendee.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">编辑</el-button>
        <el-button type="danger" plain @click="handleDelete">删除</el-button>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="编辑账号" width="480px">
      <el-form label-width="80px">
        <el-form-item label="邮箱" required :error="emailError">
          <el-input v-model="form.email" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="手机" required :error="phoneError">
          <el-input v-model="form.phone" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="新密码" :error="passwordErrorMsg">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.page-header h1 {
  margin: 0;
  font-size: 1.375rem;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
