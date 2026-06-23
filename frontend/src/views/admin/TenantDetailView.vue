<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditTenant, deleteTenant, getTenant, updateTenant } from '@/api/admin'
import { validEmail } from '@/utils/account'
import type { Tenant } from '@/types'

const route = useRoute()
const router = useRouter()
const tenantId = computed(() => String(route.params.id))
const loading = ref(false)
const submitting = ref(false)
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
  PENDING: { label: '???', type: 'warning' },
  APPROVED: { label: '???', type: 'success' },
  REJECTED: { label: '???', type: 'danger' },
}

async function load() {
  loading.value = true
  try {
    tenant.value = await getTenant(tenantId.value)
  } catch {
    tenant.value = null
  } finally {
    loading.value = false
  }
}

async function handleAudit(status: 'APPROVED' | 'REJECTED') {
  if (!tenant.value) return
  const action = status === 'APPROVED' ? '??' : '??'
  await ElMessageBox.confirm(`??${action}???${tenant.value.name}??`, '????')
  try {
    await auditTenant(tenant.value.id, status)
    tenant.value.status = status
    ElMessage.success(`?${action}`)
  } catch {
    return
  }
}

async function handleDelete() {
  if (!tenant.value) return
  await ElMessageBox.confirm(`?????${tenant.value.name}??`, '????', { type: 'warning' })
  try {
    await deleteTenant(tenant.value.id)
    ElMessage.success('???')
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
    return ElMessage.warning('??????')
  }
  if (form.value.contactEmail?.trim() && !validEmail(form.value.contactEmail)) {
    return ElMessage.warning('?????????')
  }
  submitting.value = true
  try {
    await updateTenant(tenant.value.id, form.value)
    ElMessage.success('???')
    Object.assign(tenant.value, form.value)
    dialogVisible.value = false
  } catch {
    return
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/admin/tenants')">? ????</el-button>
    </div>
    <template v-if="tenant">
      <div class="page-header">
        <h1>{{ tenant.name }}</h1>
        <el-tag :type="statusMap[tenant.status]?.type">{{ statusMap[tenant.status]?.label }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="???">{{ tenant.contactName }}</el-descriptions-item>
        <el-descriptions-item label="????">{{ tenant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="????">{{ tenant.contactEmail || '?' }}</el-descriptions-item>
        <el-descriptions-item label="??">{{ tenant.address || '?' }}</el-descriptions-item>
        <el-descriptions-item label="????">{{ tenant.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="????">{{ tenant.updatedAt || '?' }}</el-descriptions-item>
        <el-descriptions-item label="??" :span="2">{{ tenant.remark || '?' }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">??</el-button>
        <template v-if="tenant.status === 'PENDING'">
          <el-button type="primary" @click="handleAudit('APPROVED')">??</el-button>
          <el-button type="danger" @click="handleAudit('REJECTED')">??</el-button>
        </template>
        <el-button type="danger" plain @click="handleDelete">??</el-button>
      </div>
    </template>

    <el-dialog v-model="dialogVisible" title="????" width="520px">
      <el-form label-width="90px">
        <el-form-item label="????" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="???" required>
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="????" required>
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="????">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="??">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="??">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">??</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">??</el-button>
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
