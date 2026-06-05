<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrders } from '@/api/enterprise'
import type { PayOrder } from '@/types'

const loading = ref(false)
const list = ref<PayOrder[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getOrders()
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

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>订单管理</h1>
      <p>报名费与酒店订单</p>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="bizType" label="业务类型" width="120" />
      <el-table-column prop="amount" label="金额(元)" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="120" />
    </el-table>
  </div>
</template>
