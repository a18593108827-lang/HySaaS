<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayOrder, getMyOrders } from '@/api/portal'
import type { PayOrder } from '@/types'

const loading = ref(false)
const list = ref<PayOrder[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getMyOrders()
    list.value = res.records
  } catch {
    list.value = [
      { id: 1, orderNo: 'P20260601001', bizType: 'REGISTRATION', amount: 500, status: 'PAID', createdAt: '2026-06-01' },
      { id: 2, orderNo: 'P20260602002', bizType: 'HOTEL', amount: 680, status: 'PENDING', createdAt: '2026-06-02' },
    ]
  } finally {
    loading.value = false
  }
}

async function handlePay(row: PayOrder) {
  try {
    const res = await createPayOrder({ bizType: row.bizType, bizId: row.id })
    if (res.payUrl) window.open(res.payUrl, '_blank')
    ElMessage.success('已跳转支付')
  } catch {
    ElMessage.success('演示：已跳转支付宝')
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>我的订单</h1>
      <p>报名费与酒店预订订单</p>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="bizType" label="类型" width="120" />
      <el-table-column prop="amount" label="金额(元)" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="120" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" link type="primary" @click="handlePay(row)">去支付</el-button>
          <el-button v-if="row.status === 'PAID'" link @click="$router.push('/portal/orders')">申请发票</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
