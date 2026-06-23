<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getTenants, getUsers, updateUser } from '@/api/admin'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import { validateEmail, validatePassword, validatePhone } from '@/utils/account'
import type { AdminUser, UserType } from '@/types'

const router = useRouter()
const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const submitting = ref(false)
const list = ref<AdminUser[]>([])
const tenantOptions = ref<{ id: number; name: string }[]>([])
const userTypeFilter = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | string | null>(null)
const form = ref({
  email: '',
  phone: '',
  nickname: '',
  userType: 'ATTENDEE' as UserType,
  tenantId: undefined as number | undefined,
  password: '',
  status: 'ENABLED' as AdminUser['status'],
})

const userTypeMap: Record<UserType, string> = {
  PLATFORM: '平台',
  ENTERPRISE: '企业',
  ATTENDEE: '参会',
}

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const needTenant = computed(() => form.value.userType === 'ENTERPRISE')
const emailError = ref('')
const phoneError = ref('')
const passwordErrorMsg = ref('')

async function loadTenants() {
  try {
    const res = await getTenants({ status: 'APPROVED' })
    tenantOptions.value = res.records.map((t) => ({ id: t.id, name: t.name }))
  } catch {
    tenantOptions.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const res = await getUsers({
      userType: userTypeFilter.value || undefined,
      page: page.value,
      size: size.value,
    })
    list.value = res.records
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  resetPage()
  load()
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
  form.value = { email: '', phone: '', nickname: '', userType: 'ATTENDEE', tenantId: undefined, password: '', status: 'ENABLED' }
  clearFieldErrors()
  dialogVisible.value = true
}

function openEdit(row: AdminUser) {
  editingId.value = row.id
  form.value = {
    email: row.email || row.username,
    phone: row.phone || '',
    nickname: row.nickname,
    userType: row.userType,
    tenantId: row.tenantId,
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
    if (list.value.some((u) => (u.email || u.username) === email)) {
      emailError.value = '邮箱已被使用'
      return
    }
  } else if (form.value.password) {
    passwordErrorMsg.value = validatePassword(form.value.password, false)
    if (passwordErrorMsg.value) return
  }
  if (form.value.userType === 'ENTERPRISE' && !form.value.tenantId) {
    return ElMessage.warning('企业用户请选择所属租户')
  }
  const payload = {
    email: form.value.email.trim().toLowerCase(),
    phone: form.value.phone.trim(),
    nickname: form.value.nickname,
    userType: form.value.userType,
    tenantId: form.value.userType === 'ENTERPRISE' ? form.value.tenantId : undefined,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  if (editingId.value) {
    submitting.value = true
    try {
      await updateUser(editingId.value, payload)
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
      await createUser(payload)
      ElMessage.success('用户已创建')
      await load()
      dialogVisible.value = false
    } catch {
      return
    } finally {
      submitting.value = false
    }
  }
}

async function handleDelete(row: AdminUser) {
  await ElMessageBox.confirm(`删除用户「${row.username}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('已删除')
    list.value = list.value.filter((u) => u.id !== row.id)
  } catch {
    return
  }
}

onMounted(() => {
  loadTenants()
  load()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理平台、企业、参会三类账号</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新建用户</el-button>
      <el-select v-model="userTypeFilter" placeholder="全部类型" clearable style="width: 120px" @change="onFilterChange">
        <el-option label="平台" value="PLATFORM" />
        <el-option label="企业" value="ENTERPRISE" />
        <el-option label="参会" value="ATTENDEE" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无用户" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || row.username }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="nickname" label="昵称" width="110" />
      <el-table-column prop="userType" label="类型" width="80">
        <template #default="{ row }">{{ userTypeMap[row.userType as UserType] }}</template>
      </el-table-column>
      <el-table-column prop="tenantName" label="所属租户" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.tenantName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="110" />
      <el-table-column label="操作" min-width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/users/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新建用户'" width="480px">
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
        <el-form-item label="类型" required>
          <el-select v-model="form.userType" style="width: 100%" :disabled="!!editingId">
            <el-option label="平台" value="PLATFORM" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="参会" value="ATTENDEE" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="needTenant" label="租户" required>
          <el-select v-model="form.tenantId" placeholder="选择租户" style="width: 100%">
            <el-option v-for="t in tenantOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
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
