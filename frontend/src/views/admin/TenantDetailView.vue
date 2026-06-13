<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditTenant, deleteTenant, getTenant, updateTenant } from '@/api/admin'
import type { Tenant } from '@/types'

const route = useRoute()
const router = useRouter()
const tenantId = computed(() => String(route.params.id))
const loading = ref(false)
const tenant = ref<Tenant | null>(null)
const dialogVisible = ref(false)
const form = ref({
  name: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  remark: '',
})

const statusMap: Record<string, { label: string; type: '' | 'success' | 'warning' | 'danger' }> = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
}

const demoMap: Record<string, Tenant> = {
  '1': { id: 1, name: '华东医学会', contactName: '张敏', contactPhone: '13800138001', contactEmail: 'zhang@example.com', address: '上海市黄浦区', remark: '医学类大型会议主办方', status: 'PENDING', createdAt: '2026-06-01' },
  '2': { id: 2, name: '深圳创新峰会', contactName: '李强', contactPhone: '13900139002', contactEmail: 'li@example.com', status: 'APPROVED', createdAt: '2026-05-28', updatedAt: '2026-05-29' },
}

async function load() {
  loading.value = true
  try {
    tenant.value = await getTenant(tenantId.value)
  } catch {
    tenant.value = demoMap[tenantId.value] ?? {
      id: tenantId.value,
      name: `租户 #${tenantId.value}`,
      contactName: '—',
      contactPhone: '—',
      status: 'PENDING',
      createdAt: '—',
    }
  } finally {
    loading.value = false
  }
}

async function handleAudit(status: 'APPROVED' | 'REJECTED') {
  if (!tenant.value) return
  const action = status === 'APPROVED' ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}租户「${tenant.value.name}」？`, '审核确认')
  try {
    await auditTenant(tenant.value.id, status)
    tenant.value.status = status
    ElMessage.success(`已${action}`)
  } catch {
    return
  }
}

async function handleDelete() {
  if (!tenant.value) return
  await ElMessageBox.confirm(`删除租户「${tenant.value.name}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteTenant(tenant.value.id)
    ElMessage.success('已删除')
    router.push('/admin/tenants')
  } catch {
    return
  }
}

function openEdit() {
  if (!tenant.value) return
  form.value = {
    name: tenant.value.name,
    contactName: tenant.value.contactName,
    contactPhone: tenant.value.contactPhone,
    contactEmail: tenant.value.contactEmail || '',
    address: tenant.value.address || '',
    remark: tenant.value.remark || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!tenant.value) return
  if (!form.value.name || !form.value.contactName || !form.value.contactPhone) {
    return ElMessage.warning('请填写必填项')
  }
  try {
    await updateTenant(tenant.value.id, form.value)
    ElMessage.success('已保存')
    Object.assign(tenant.value, form.value)
    dialogVisible.value = false
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/admin/tenants')">← 返回列表</el-button>
    </div>
    <template v-if="tenant">
      <div class="page-header">
        <h1>{{ tenant.name }}</h1>
        <el-tag :type="statusMap[tenant.status]?.type">{{ statusMap[tenant.status]?.label }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="联系人">{{ tenant.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ tenant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ tenant.contactEmail || '—' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ tenant.address || '—' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ tenant.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ tenant.updatedAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ tenant.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">编辑</el-button>
        <template v-if="tenant.status === 'PENDING'">
          <el-button type="primary" @click="handleAudit('APPROVED')">通过</el-button>
          <el-button type="danger" @click="handleAudit('REJECTED')">拒绝</el-button>
        </template>
        <el-button type="danger" plain @click="handleDelete">删除</el-button>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="编辑租户" width="520px">
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
