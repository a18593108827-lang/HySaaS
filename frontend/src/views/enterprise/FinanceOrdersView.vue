<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrders } from '@/api/enterprise'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import { payBizTypeMap, payStatusMap } from '@/utils/labels'
import type { PayOrder } from '@/types'

const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const list = ref<PayOrder[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, size: size.value })
    list.value = res.records
    total.value = res.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>订单管理</h1>
      <p>报名费与酒店订单</p>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无订单" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="bizType" label="业务类型" width="120">
        <template #default="{ row }">{{ payBizTypeMap[row.bizType] || row.bizType }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额(元)" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="payStatusMap[row.status]?.type">{{ payStatusMap[row.status]?.label || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="120" />
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />
  </div>
</template>
