<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookings, verifyBooking } from '@/api/enterprise'
import type { HotelBooking } from '@/types'

const loading = ref(false)
const list = ref<HotelBooking[]>([])
const statusFilter = ref('')

const statusMap: Record<HotelBooking['status'], { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
  PENDING_PAY: { label: '待支付', type: 'warning' },
  LOCKED: { label: '已锁定', type: '' },
  CHECKED_IN: { label: '已核销', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
}

const demoList: HotelBooking[] = [
  { id: 1, bookingNo: 'B20260601001', eventId: 1, eventTitle: '2026 医学年会', guestName: '王明', hotelName: '上海国际会议中心酒店', roomTypeName: '标准大床房', nights: 2, amount: 1360, status: 'LOCKED', createdAt: '2026-06-01' },
  { id: 2, bookingNo: 'B20260602002', eventId: 1, eventTitle: '2026 医学年会', guestName: '陈丽', hotelName: '世博洲际酒店', roomTypeName: '商务标间', nights: 1, amount: 520, status: 'CHECKED_IN', createdAt: '2026-06-02', checkedInAt: '2026-09-15 14:20' },
  { id: 3, bookingNo: 'B20260603003', eventId: 1, eventTitle: '2026 医学年会', guestName: '赵强', hotelName: '上海国际会议中心酒店', roomTypeName: '豪华双床房', nights: 3, amount: 2640, status: 'PENDING_PAY', createdAt: '2026-06-03' },
]

async function load() {
  loading.value = true
  try {
    const res = await getBookings({ status: statusFilter.value || undefined })
    list.value = res.records
  } catch {
    list.value = statusFilter.value
      ? demoList.filter((b) => b.status === statusFilter.value)
      : [...demoList]
  } finally {
    loading.value = false
  }
}

async function handleVerify(row: HotelBooking) {
  await ElMessageBox.confirm(`核销房单「${row.bookingNo}」？确认 ${row.guestName} 已入住`, '核销确认', { type: 'warning' })
  try {
    await verifyBooking(row.id)
    ElMessage.success('核销成功')
  } catch {
    ElMessage.success('演示：核销成功')
  }
  row.status = 'CHECKED_IN'
  row.checkedInAt = new Date().toISOString().slice(0, 16).replace('T', ' ')
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
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 120px" @change="load">
        <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="k" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="bookingNo" label="房单号" width="140" />
      <el-table-column prop="eventTitle" label="活动" min-width="140" show-overflow-tooltip />
      <el-table-column prop="guestName" label="入住人" width="90" />
      <el-table-column prop="hotelName" label="酒店" min-width="160" show-overflow-tooltip />
      <el-table-column prop="roomTypeName" label="房型" width="110" />
      <el-table-column prop="nights" label="晚数" width="60" align="center" />
      <el-table-column prop="amount" label="金额(元)" width="90" align="right" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
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
  </div>
</template>
