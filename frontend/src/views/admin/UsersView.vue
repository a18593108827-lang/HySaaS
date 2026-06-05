<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getTenants, getUsers, updateUser } from '@/api/admin'
import type { AdminUser, UserType } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<AdminUser[]>([])
const tenantOptions = ref<{ id: number; name: string }[]>([])
const userTypeFilter = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  username: '',
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

const demoList: AdminUser[] = [
  { id: 1, username: 'admin@test.com', nickname: '平台管理员', userType: 'PLATFORM', status: 'ENABLED', createdAt: '2026-01-01' },
  { id: 2, username: 'ent@test.com', nickname: '企业管理员', userType: 'ENTERPRISE', tenantId: 2, tenantName: '深圳创新峰会', status: 'ENABLED', createdAt: '2026-05-29' },
  { id: 3, username: 'user@test.com', nickname: '参会用户', userType: 'ATTENDEE', status: 'ENABLED', createdAt: '2026-06-01' },
]

async function loadTenants() {
  try {
    const res = await getTenants({ status: 'APPROVED' })
    tenantOptions.value = res.records.map((t) => ({ id: t.id, name: t.name }))
  } catch {
    tenantOptions.value = [{ id: 2, name: '深圳创新峰会' }]
  }
}

async function load() {
  loading.value = true
  try {
    const res = await getUsers({ userType: userTypeFilter.value || undefined })
    list.value = res.records
  } catch {
    list.value = userTypeFilter.value
      ? demoList.filter((u) => u.userType === userTypeFilter.value)
      : [...demoList]
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { username: '', nickname: '', userType: 'ATTENDEE', tenantId: undefined, password: '', status: 'ENABLED' }
  dialogVisible.value = true
}

function openEdit(row: AdminUser) {
  editingId.value = row.id
  form.value = {
    username: row.username,
    nickname: row.nickname,
    userType: row.userType,
    tenantId: row.tenantId,
    password: '',
    status: row.status,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.username || !form.value.nickname) return ElMessage.warning('请填写账号和昵称')
  if (!editingId.value && (!form.value.password || form.value.password.length < 6)) {
    return ElMessage.warning('密码至少 6 位')
  }
  if (form.value.userType === 'ENTERPRISE' && !form.value.tenantId) {
    return ElMessage.warning('企业用户请选择所属租户')
  }
  const payload = {
    username: form.value.username,
    nickname: form.value.nickname,
    userType: form.value.userType,
    tenantId: form.value.userType === 'ENTERPRISE' ? form.value.tenantId : undefined,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  if (editingId.value) {
    try {
      await updateUser(editingId.value, payload)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = list.value.find((u) => u.id === editingId.value)
    if (row) {
      Object.assign(row, payload)
      row.tenantName = tenantOptions.value.find((t) => t.id === row.tenantId)?.name
    }
  } else {
    try {
      await createUser(payload)
      ElMessage.success('用户已创建')
      load()
    } catch {
      list.value.unshift({
        id: Date.now(),
        ...payload,
        status: payload.status ?? 'ENABLED',
        tenantName: tenantOptions.value.find((t) => t.id === payload.tenantId)?.name,
        createdAt: new Date().toISOString().slice(0, 10),
      } as AdminUser)
      ElMessage.success('演示：用户已创建')
    }
  }
  dialogVisible.value = false
}

async function handleDelete(row: AdminUser) {
  await ElMessageBox.confirm(`删除用户「${row.username}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  list.value = list.value.filter((u) => u.id !== row.id)
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
      <el-select v-model="userTypeFilter" placeholder="全部类型" clearable style="width: 120px" @change="load">
        <el-option label="平台" value="PLATFORM" />
        <el-option label="企业" value="ENTERPRISE" />
        <el-option label="参会" value="ATTENDEE" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="username" label="账号" min-width="140" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新建用户'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="账号" required>
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="邮箱或手机号" />
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
        <el-form-item :label="editingId ? '新密码' : '密码'" :required="!editingId">
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
        <el-button type="primary" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
