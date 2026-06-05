<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAttendee, deleteAttendee, getAttendees, updateAttendee } from '@/api/enterprise'
import type { EnterpriseAttendee } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<EnterpriseAttendee[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  username: '',
  nickname: '',
  password: '',
  status: 'ENABLED' as EnterpriseAttendee['status'],
})

const statusMap: Record<string, { label: string; type: '' | 'success' | 'danger' }> = {
  ENABLED: { label: '正常', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const demoList: EnterpriseAttendee[] = [
  { id: 101, username: 'user@test.com', nickname: '参会用户', status: 'ENABLED', createdAt: '2026-06-01' },
  { id: 102, username: 'wang@example.com', nickname: '王明', status: 'ENABLED', createdAt: '2026-06-03' },
]

async function load() {
  loading.value = true
  try {
    const res = await getAttendees()
    list.value = res.records
  } catch {
    list.value = [...demoList]
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { username: '', nickname: '', password: '', status: 'ENABLED' }
  dialogVisible.value = true
}

function openEdit(row: EnterpriseAttendee) {
  editingId.value = row.id
  form.value = { username: row.username, nickname: row.nickname, password: '', status: row.status }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.username || !form.value.nickname) return ElMessage.warning('请填写账号和昵称')
  if (!editingId.value && (!form.value.password || form.value.password.length < 6)) {
    return ElMessage.warning('密码至少 6 位')
  }
  const payload = {
    username: form.value.username,
    nickname: form.value.nickname,
    status: form.value.status,
    ...(form.value.password ? { password: form.value.password } : {}),
  }
  if (editingId.value) {
    try {
      await updateAttendee(editingId.value, payload)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = list.value.find((m) => m.id === editingId.value)
    if (row) Object.assign(row, payload)
  } else {
    try {
      await createAttendee(payload)
      ElMessage.success('账号已创建')
      load()
    } catch {
      list.value.unshift({
        id: Date.now(),
        ...payload,
        status: payload.status ?? 'ENABLED',
        createdAt: new Date().toISOString().slice(0, 10),
      } as EnterpriseAttendee)
      ElMessage.success('演示：账号已创建')
    }
  }
  dialogVisible.value = false
}

async function handleDelete(row: EnterpriseAttendee) {
  await ElMessageBox.confirm(`删除参会账号「${row.nickname}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteAttendee(row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  list.value = list.value.filter((m) => m.id !== row.id)
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
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="username" label="账号" min-width="160" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="110" />
      <el-table-column label="操作" min-width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/enterprise/attendees/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑账号' : '新建账号'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="账号" required>
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="邮箱或手机号" />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" />
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
