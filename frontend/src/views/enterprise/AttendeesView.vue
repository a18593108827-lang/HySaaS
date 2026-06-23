<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAttendee, deleteAttendee, getAttendees, updateAttendee } from '@/api/enterprise'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import { validateEmail, validatePassword, validatePhone } from '@/utils/account'
import type { EnterpriseAttendee } from '@/types'

const router = useRouter()
const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const submitting = ref(false)
const list = ref<EnterpriseAttendee[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  email: '',
  phone: '',
  nickname: '',
  password: '',
  status: 'ENABLED' as EnterpriseAttendee['status'],
})

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const emailError = ref('')
const phoneError = ref('')
const passwordErrorMsg = ref('')

async function load() {
  loading.value = true
  try {
    const res = await getAttendees({ page: page.value, size: size.value })
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

function clearFieldErrors() {
  emailError.value = ''
  phoneError.value = ''
  passwordErrorMsg.value = ''
}

function openCreate() {
  editingId.value = null
  form.value = { email: '', phone: '', nickname: '', password: '', status: 'ENABLED' }
  clearFieldErrors()
  dialogVisible.value = true
}

function openEdit(row: EnterpriseAttendee) {
  editingId.value = row.id
  form.value = {
    email: row.email || row.username,
    phone: row.phone || '',
    nickname: row.nickname,
    password: '',
    status: row.status,
  }
  clearFieldErrors()
  dialogVisible.value = true
}

async function handleSubmit() {
  clearFieldErrors()
  if (!form.value.nickname) return ElMessage.warning('请填写昵称')
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (emailError.value || phoneError.value) return
  if (!editingId.value) {
    passwordErrorMsg.value = validatePassword(form.value.password, true)
    if (passwordErrorMsg.value) return
    const email = form.value.email.trim().toLowerCase()
    if (list.value.some((a) => (a.email || a.username) === email)) {
      emailError.value = '邮箱已被使用'
      return
    }
  } else if (form.value.password) {
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
  if (editingId.value) {
    submitting.value = true
    try {
      await updateAttendee(editingId.value, payload)
      ElMessage.success('已保存')
      await load()
      dialogVisible.value = false
    } catch {
      return
    } finally {
      submitting.value = false
    }
  } else {
    submitting.value = true
    try {
      await createAttendee(payload)
      ElMessage.success('账号已创建')
      await load()
      dialogVisible.value = false
    } catch {
      return
    } finally {
      submitting.value = false
    }
  }
}

async function handleDelete(row: EnterpriseAttendee) {
  await ElMessageBox.confirm(`删除参会账号「${row.nickname}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteAttendee(row.id)
    ElMessage.success('已删除')
    list.value = list.value.filter((m) => m.id !== row.id)
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>参会账号</h1>
      <p>管理本租户下的参会登录账号；单场活动报名在「活动管理 → 报名审核」</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新建账号</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无参会账号" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || row.username }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="110" />
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/enterprise/attendees/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑账号' : '新建账号'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="邮箱" required :error="emailError">
          <el-input v-model="form.email" placeholder="登录账号，同时用于联系" />
        </el-form-item>
        <el-form-item label="手机" required :error="phoneError">
          <el-input v-model="form.phone" placeholder="11 位手机号，可用于登录" />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="editingId ? '新密码' : '密码'" :required="!editingId" :error="passwordErrorMsg">
          <el-input v-model="form.password" type="password" show-password :placeholder="editingId ? '留空则不修改' : '至少 6 位'" />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
