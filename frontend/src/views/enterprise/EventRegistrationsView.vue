<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditRegistration, getRegistrations } from '@/api/enterprise'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import type { Registration } from '@/types'

const route = useRoute()
const router = useRouter()
const eventId = String(route.params.id)
const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const list = ref<Registration[]>([])
const statusFilter = ref('')

const statusMap: Record<string, { label: string; type: '' | 'success' | 'warning' | 'danger' }> = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
}

async function load() {
  loading.value = true
  try {
    const res = await getRegistrations(eventId, {
      status: statusFilter.value || undefined,
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

async function handleAudit(row: Registration, status: 'APPROVED' | 'REJECTED') {
  const action = status === 'APPROVED' ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}「${row.name}」的报名？`, '审核确认')
  try {
    await auditRegistration(row.id, status)
    row.status = status
    ElMessage.success(`已${action}，邮件已发送`)
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/enterprise/events')">← 返回活动列表</el-button>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="onFilterChange">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无报名记录" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="memberType" label="会员类型" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="primary" @click="handleAudit(row, 'APPROVED')">通过</el-button>
            <el-button link type="danger" @click="handleAudit(row, 'REJECTED')">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />
  </div>
</template>
