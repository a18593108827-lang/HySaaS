<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteUser, getUser, updateUser } from '@/api/admin'
import type { AdminUser, UserType } from '@/types'

const route = useRoute()
const router = useRouter()
const userId = computed(() => Number(route.params.id))
const loading = ref(false)
const user = ref<AdminUser | null>(null)
const dialogVisible = ref(false)
const form = ref({ nickname: '', password: '', status: 'ENABLED' as AdminUser['status'] })

const userTypeMap: Record<UserType, string> = {
  PLATFORM: '平台',
  ENTERPRISE: '企业',
  ATTENDEE: '参会',
}

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const demoMap: Record<number, AdminUser> = {
  1: { id: 1, username: 'admin@test.com', nickname: '平台管理员', userType: 'PLATFORM', status: 'ENABLED', createdAt: '2026-01-01' },
  2: { id: 2, username: 'ent@test.com', nickname: '企业管理员', userType: 'ENTERPRISE', tenantId: 2, tenantName: '深圳创新峰会', status: 'ENABLED', createdAt: '2026-05-29' },
  3: { id: 3, username: 'user@test.com', nickname: '参会用户', userType: 'ATTENDEE', status: 'ENABLED', createdAt: '2026-06-01' },
}

async function load() {
  loading.value = true
  try {
    user.value = await getUser(userId.value)
  } catch {
    user.value = demoMap[userId.value] ?? {
      id: userId.value,
      username: `user${userId.value}@test.com`,
      nickname: '演示用户',
      userType: 'ATTENDEE',
      status: 'ENABLED',
      createdAt: '—',
    }
  } finally {
    loading.value = false
  }
}

function openEdit() {
  if (!user.value) return
  form.value = { nickname: user.value.nickname, password: '', status: user.value.status }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!user.value || !form.value.nickname) return ElMessage.warning('请填写昵称')
  if (form.value.password && form.value.password.length < 6) return ElMessage.warning('密码至少 6 位')
  const payload = { nickname: form.value.nickname, status: form.value.status, ...(form.value.password ? { password: form.value.password } : {}) }
  try {
    await updateUser(user.value.id, payload)
    ElMessage.success('已保存')
  } catch {
    ElMessage.success('演示：已保存')
  }
  Object.assign(user.value, payload)
  dialogVisible.value = false
}

async function handleDelete() {
  if (!user.value) return
  await ElMessageBox.confirm(`删除用户「${user.value.username}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteUser(user.value.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  router.push('/admin/users')
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/admin/users')">← 返回列表</el-button>
    </div>
    <template v-if="user">
      <div class="page-header">
        <h1>{{ user.nickname }}</h1>
        <el-tag>{{ userTypeMap[user.userType] }}</el-tag>
        <el-tag :type="statusMap[user.status]?.type">{{ statusMap[user.status]?.label }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账号">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ userTypeMap[user.userType] }}</el-descriptions-item>
        <el-descriptions-item label="所属租户">{{ user.tenantName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ user.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">编辑</el-button>
        <el-button type="danger" plain @click="handleDelete">删除</el-button>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="440px">
      <el-form label-width="80px">
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="新密码">
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
