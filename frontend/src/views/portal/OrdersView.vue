<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyInvoice, createPayOrder, getMyOrders, mockPay } from '@/api/portal'
import ListPagination from '@/components/ListPagination.vue'
import { usePagination } from '@/composables/usePagination'
import { isMobilePayChannel, launchPay } from '@/utils/pay'
import { invoiceStatusMap, payBizTypeMap, payStatusMap } from '@/utils/labels'
import type { PayOrder } from '@/types'

const { page, size, total, resetPage } = usePagination()
const loading = ref(false)
const list = ref<PayOrder[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const currentOrder = ref<PayOrder | null>(null)
const form = ref({ title: '', taxNo: '', email: '' })

async function load() {
  loading.value = true
  try {
    const res = await getMyOrders({ page: page.value, size: size.value })
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

async function handlePay(row: PayOrder) {
  try {
    const mode = await launchPay(
      () => createPayOrder({
        bizType: row.bizType,
        bizId: row.id,
        channel: isMobilePayChannel() ? 'wap' : 'page',
      }),
      () => mockPay(row.id),
    )
    if (mode === 'alipay') {
      ElMessage.success('正在跳转支付宝…')
      return
    }
    ElMessage.success('支付成功')
    await load()
  } catch {
    return
  }
}

function openInvoice(row: PayOrder) {
  currentOrder.value = row
  form.value = { title: '', taxNo: '', email: '' }
  dialogVisible.value = true
}

async function handleApplyInvoice() {
  if (!currentOrder.value) return
  if (!form.value.title || !form.value.taxNo || !form.value.email) {
    return ElMessage.warning('请填写发票抬头、税号和接收邮箱')
  }
  submitting.value = true
  try {
    await applyInvoice({ orderId: currentOrder.value.id, ...form.value })
    ElMessage.success('发票申请已提交')
    currentOrder.value.invoiceStatus = 'APPLYING'
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
  <div>
    <div class="page-header">
      <h1>我的订单</h1>
      <p>报名费与酒店预订订单</p>
    </div>
    <div class="toolbar">
      <el-button @click="load">刷新</el-button>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无订单" />
    <el-table v-else v-loading="loading" :data="list" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="bizType" label="类型" width="100">
        <template #default="{ row }">{{ payBizTypeMap[row.bizType] || row.bizType }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额(元)" width="100" align="right" />
      <el-table-column prop="status" label="支付状态" width="100">
        <template #default="{ row }">
          <el-tag :type="payStatusMap[row.status]?.type">{{ payStatusMap[row.status]?.label || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发票" width="90">
        <template #default="{ row }">
          <span v-if="row.status === 'PAID'">{{ invoiceStatusMap[row.invoiceStatus || 'NONE'] }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" link type="primary" @click="handlePay(row)">去支付</el-button>
          <el-button
            v-if="row.status === 'PAID' && (!row.invoiceStatus || row.invoiceStatus === 'NONE')"
            link
            type="primary"
            @click="openInvoice(row)"
          >
            申请发票
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <ListPagination :total="total" :page="page" :size="size" @change="onPageChange" @size-change="onSizeChange" />

    <el-dialog v-model="dialogVisible" title="申请发票" width="480px">
      <p v-if="currentOrder" class="order-hint">
        订单 {{ currentOrder.orderNo }} · ¥{{ currentOrder.amount }}
      </p>
      <el-form label-width="80px">
        <el-form-item label="发票抬头" required>
          <el-input v-model="form.title" placeholder="单位全称或个人姓名" />
        </el-form-item>
        <el-form-item label="税号" required>
          <el-input v-model="form.taxNo" placeholder="纳税人识别号" />
        </el-form-item>
        <el-form-item label="接收邮箱" required>
          <el-input v-model="form.email" type="email" placeholder="用于接收发票下载链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApplyInvoice">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-hint {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: var(--muted);
}
</style>
