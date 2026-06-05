<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInvoices } from '@/api/enterprise'

interface Invoice {
  id: number
  orderNo: string
  title: string
  amount: number
  status: string
  createdAt: string
}

const loading = ref(false)
const list = ref<Invoice[]>([])

async function load() {
  loading.value = true
  try {
    list.value = await getInvoices()
  } catch {
    list.value = [
      { id: 1, orderNo: 'P20260601001', title: '华东医学会', amount: 500, status: 'ISSUED', createdAt: '2026-06-01' },
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
      <h1>发票管理</h1>
      <p>票点云开票记录</p>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="orderNo" label="关联订单" width="160" />
      <el-table-column prop="title" label="发票抬头" min-width="160" />
      <el-table-column prop="amount" label="金额(元)" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="开票时间" width="120" />
      <el-table-column label="操作" width="100">
        <template #default>
          <el-button link type="primary">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
