<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookings, verifyBooking } from '@/api/enterprise'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import type { HotelBooking } from '@/types'

const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const list = ref<HotelBooking[]>([])
const statusFilter = ref('')

const statusMap: Record<HotelBooking['status'], { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
  PENDING_PAY: { label: '待支付', type: 'warning' },
  LOCKED: { label: '已锁定', type: '' },
  CHECKED_IN: { label: '已核销', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
}

async function load() {
  loading.value = true
  try {
    const res = await getBookings({
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

async function handleVerify(row: HotelBooking) {
  await ElMessageBox.confirm(`核销房单「${row.bookingNo}」？确认 ${row.guestName} 已入住`, '核销确认', { type: 'warning' })
  try {
    await verifyBooking(row.id)
    ElMessage.success('核销成功')
    row.status = 'CHECKED_IN'
    row.checkedInAt = new Date().toISOString().slice(0, 10)
  } catch {
    return
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>房单核销</h1>
      <p>查看酒店预订房单，现场入住后核销</p>
    </div>
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 120px" @change="onFilterChange">
        <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="k" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无房单" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="bookingNo" label="房单号" width="140" />
      <el-table-column prop="eventTitle" label="活动" min-width="140" show-overflow-tooltip />
      <el-table-column prop="guestName" label="入住人" width="90" />
      <el-table-column prop="hotelName" label="酒店" min-width="160" show-overflow-tooltip />
      <el-table-column prop="roomTypeName" label="房型" width="110" />
      <el-table-column prop="nights" label="晚数" width="60" />
      <el-table-column prop="amount" label="金额(元)" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status as HotelBooking['status']]?.type">{{ statusMap[row.status as HotelBooking['status']]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="预订时间" width="110" />
      <el-table-column prop="checkedInAt" label="核销时间" width="140" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'LOCKED'" link type="primary" @click="handleVerify(row)">核销</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />
  </div>
</template>
