<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMember, deleteMember, getMembers, updateMember } from '@/api/enterprise'
import { validateEmail, validatePassword, validatePhone } from '@/utils/account'
import type { EnterpriseMember } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<EnterpriseMember[]>([])
const roleFilter = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  email: '',
  phone: '',
  nickname: '',
  roles: [] as string[],
  password: '',
  status: 'ENABLED' as EnterpriseMember['status'],
})

const roleOptions = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'EVENT_STAFF', label: '会务' },
  { value: 'FINANCE', label: '财务' },
  { value: 'EXPERT', label: '专家' },
]

const roleMap = Object.fromEntries(roleOptions.map((r) => [r.value, r.label]))

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const emailError = ref('')
const phoneError = ref('')
const passwordErrorMsg = ref('')

const demoList: EnterpriseMember[] = [
  { id: 1, username: 'ent@test.com', email: 'ent@test.com', phone: '13800000002', nickname: '企业管理员', roles: ['ADMIN'], status: 'ENABLED', createdAt: '2026-05-29' },
  { id: 2, username: 'staff@test.com', email: 'staff@test.com', phone: '13800001112', nickname: '会务小李', roles: ['EVENT_STAFF'], status: 'ENABLED', createdAt: '2026-06-01' },
  { id: 3, username: 'expert@test.com', email: 'expert@test.com', phone: '13800001113', nickname: '评审专家王', roles: ['EXPERT'], status: 'ENABLED', createdAt: '2026-06-02' },
]

async function load() {
  loading.value = true
  try {
    const res = await getMembers({ role: roleFilter.value || undefined })
    list.value = res.records
  } catch {
    list.value = roleFilter.value
      ? demoList.filter((m) => m.roles.includes(roleFilter.value))
      : [...demoList]
  } finally {
    loading.value = false
  }
}

function clearFieldErrors() {
  emailError.value = ''
  phoneError.value = ''
  passwordErrorMsg.value = ''
}

function openCreate() {
  editingId.value = null
  form.value = { email: '', phone: '', nickname: '', roles: ['EVENT_STAFF'], password: '', status: 'ENABLED' }
  clearFieldErrors()
  dialogVisible.value = true
}

function openEdit(row: EnterpriseMember) {
  editingId.value = row.id
  form.value = {
    email: row.email || row.username,
    phone: row.phone || '',
    nickname: row.nickname,
    roles: [...row.roles],
    password: '',
    status: row.status,
  }
  clearFieldErrors()
  dialogVisible.value = true
}

async function handleSubmit() {
  clearFieldErrors()
  if (!form.value.nickname) return ElMessage.warning('请填写昵称')
  if (!form.value.roles.length) return ElMessage.warning('请选择至少一个角色')
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (emailError.value || phoneError.value) return
  if (!editingId.value) {
    passwordErrorMsg.value = validatePassword(form.value.password, true)
    if (passwordErrorMsg.value) return
    const email = form.value.email.trim().toLowerCase()
    if (list.value.some((m) => (m.email || m.username) === email)) {
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
    roles: form.value.roles,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  if (editingId.value) {
    try {
      await updateMember(editingId.value, payload)
      ElMessage.success('已保存')
    } catch {
      return
    }
    await load()
    dialogVisible.value = false
  } else {
    try {
      await createMember(payload)
      ElMessage.success('成员已创建')
      await load()
      dialogVisible.value = false
    } catch {
      return
    }
  }
}

async function handleDelete(row: EnterpriseMember) {
  await ElMessageBox.confirm(`删除成员「${row.nickname}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteMember(row.id)
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
      <h1>成员管理</h1>
      <p>管理本租户下的企业账号与角色</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新建成员</el-button>
      <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width: 120px" @change="load">
        <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || row.username }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="nickname" label="昵称" width="110" />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 4px">
            {{ roleMap[role] || role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="110" />
      <el-table-column label="操作" min-width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/enterprise/members/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑成员' : '新建成员'" width="480px">
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
        <el-form-item label="角色" required>
          <el-select v-model="form.roles" multiple style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
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
        <el-button type="primary" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
