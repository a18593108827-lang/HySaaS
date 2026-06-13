<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteMember, getMember, updateMember } from '@/api/enterprise'
import { validateEmail, validatePassword, validatePhone } from '@/utils/account'
import type { EnterpriseMember } from '@/types'

const route = useRoute()
const router = useRouter()
const memberId = computed(() => String(route.params.id))
const loading = ref(false)
const member = ref<EnterpriseMember | null>(null)
const dialogVisible = ref(false)
const form = ref({
  email: '',
  phone: '',
  nickname: '',
  roles: [] as string[],
  password: '',
  status: 'ENABLED' as EnterpriseMember['status'],
})
const emailError = ref('')
const phoneError = ref('')
const passwordErrorMsg = ref('')

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

const demoMap: Record<string, EnterpriseMember> = {
  '1': { id: 1, username: 'ent@test.com', email: 'ent@test.com', phone: '13800000002', nickname: '企业管理员', roles: ['ADMIN'], status: 'ENABLED', createdAt: '2026-05-29' },
  '2': { id: 2, username: 'staff@test.com', email: 'staff@test.com', phone: '13800001112', nickname: '会务小李', roles: ['EVENT_STAFF'], status: 'ENABLED', createdAt: '2026-06-01' },
  '3': { id: 3, username: 'expert@test.com', email: 'expert@test.com', phone: '13800001113', nickname: '评审专家王', roles: ['EXPERT'], status: 'ENABLED', createdAt: '2026-06-02' },
}

async function load() {
  loading.value = true
  try {
    member.value = await getMember(memberId.value)
  } catch {
    member.value = demoMap[memberId.value] ?? {
      id: memberId.value as unknown as number,
      username: `member${memberId.value}@test.com`,
      email: `member${memberId.value}@test.com`,
      phone: '',
      nickname: '演示成员',
      roles: ['EVENT_STAFF'],
      status: 'ENABLED',
      createdAt: '—',
    }
  } finally {
    loading.value = false
  }
}

function openEdit() {
  if (!member.value) return
  form.value = {
    email: member.value.email || member.value.username,
    phone: member.value.phone || '',
    nickname: member.value.nickname,
    roles: [...member.value.roles],
    password: '',
    status: member.value.status,
  }
  emailError.value = ''
  phoneError.value = ''
  passwordErrorMsg.value = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!member.value || !form.value.nickname) return ElMessage.warning('请填写昵称')
  if (!form.value.roles.length) return ElMessage.warning('请选择至少一个角色')
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
    roles: form.value.roles,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  try {
    member.value = await updateMember(member.value.id, payload)
    ElMessage.success('已保存')
    dialogVisible.value = false
  } catch {
    return
  }
}

async function handleDelete() {
  if (!member.value) return
  await ElMessageBox.confirm(`删除成员「${member.value.nickname}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteMember(member.value.id)
    ElMessage.success('已删除')
    router.push('/enterprise/members')
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/enterprise/members')">← 返回列表</el-button>
    </div>
    <template v-if="member">
      <div class="page-header">
        <h1>{{ member.nickname }}</h1>
        <el-tag :type="statusMap[member.status]?.type">{{ statusMap[member.status]?.label }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="邮箱">{{ member.email || member.username }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ member.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ member.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="role in member.roles" :key="role" size="small" style="margin-right: 4px">
            {{ roleMap[role] || role }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ member.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">编辑</el-button>
        <el-button type="danger" plain @click="handleDelete">删除</el-button>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="编辑成员" width="480px">
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
        <el-form-item label="角色" required>
          <el-select v-model="form.roles" multiple style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
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
