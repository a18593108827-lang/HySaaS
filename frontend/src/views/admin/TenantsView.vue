<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditTenant, createTenant, deleteTenant, getTenants, updateTenant } from '@/api/admin'
import type { Tenant } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<Tenant[]>([])
const statusFilter = ref('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({
  name: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  remark: '',
  status: 'APPROVED' as Tenant['status'],
})

const statusMap: Record<string, { label: string; type: '' | 'success' | 'warning' | 'danger' | 'info' }> = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
}

const demoList: Tenant[] = [
  { id: 1, name: '华东医学会', contactName: '张敏', contactPhone: '13800138001', contactEmail: 'zhang@example.com', address: '上海市黄浦区', status: 'PENDING', createdAt: '2026-06-01' },
  { id: 2, name: '深圳创新峰会', contactName: '李强', contactPhone: '13900139002', contactEmail: 'li@example.com', status: 'APPROVED', createdAt: '2026-05-28' },
]

async function load() {
  loading.value = true
  try {
    const res = await getTenants({ status: statusFilter.value || undefined })
    list.value = res.records
  } catch {
    list.value = [...demoList]
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { name: '', contactName: '', contactPhone: '', contactEmail: '', address: '', remark: '', status: 'APPROVED' }
  dialogVisible.value = true
}

function openEdit(row: Tenant) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    contactName: row.contactName,
    contactPhone: row.contactPhone,
    contactEmail: row.contactEmail || '',
    address: row.address || '',
    remark: row.remark || '',
    status: row.status,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.contactName || !form.value.contactPhone) {
    return ElMessage.warning('请填写企业名称、联系人、联系电话')
  }
  const payload = { ...form.value }
  if (editingId.value) {
    try {
      await updateTenant(editingId.value, payload)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = list.value.find((e) => e.id === editingId.value)
    if (row) Object.assign(row, payload)
  } else {
    try {
      await createTenant(payload)
      ElMessage.success('租户已创建')
      load()
    } catch {
      list.value.unshift({
        id: Date.now(),
        ...payload,
        createdAt: new Date().toISOString().slice(0, 10),
      } as Tenant)
      ElMessage.success('演示：租户已创建')
    }
  }
  dialogVisible.value = false
}

async function handleDelete(row: Tenant) {
  await ElMessageBox.confirm(`删除租户「${row.name}」？此操作不可恢复`, '删除确认', { type: 'warning' })
  try {
    await deleteTenant(row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  list.value = list.value.filter((e) => e.id !== row.id)
}

async function handleAudit(row: Tenant, status: 'APPROVED' | 'REJECTED') {
  const action = status === 'APPROVED' ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}租户「${row.name}」？`, '审核确认')
  try {
    await auditTenant(row.id, status)
    ElMessage.success(`已${action}`)
  } catch {
    row.status = status
    ElMessage.success(`演示：已${action}`)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>租户管理</h1>
      <p>审核企业入驻申请，管理平台租户</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新建租户</el-button>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="load">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="name" label="企业名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="contactName" label="联系人" width="90" />
      <el-table-column prop="contactPhone" label="联系电话" width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="110" />
      <el-table-column label="操作" min-width="340" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/tenants/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="primary"
            @click="handleAudit(row, 'APPROVED')"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link
            type="danger"
            @click="handleAudit(row, 'REJECTED')"
          >
            拒绝
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑租户' : '新建租户'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="企业名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="初始状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="已通过" value="APPROVED" />
            <el-option label="待审核" value="PENDING" />
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
