<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createHotelBooking, createPayOrder, getHotelRooms, mockPay } from '@/api/portal'
import { isMobilePayChannel, launchPay } from '@/utils/pay'
import PortalBackBar from '@/components/PortalBackBar.vue'

interface RoomType {
  id: number
  name: string
  price: number
  quota: number
}

const route = useRoute()
const router = useRouter()
const eventId = String(route.params.eventId)
const loading = ref(false)
const submitting = ref(false)
const rooms = ref<RoomType[]>([])
const selected = ref<number>()
const nights = ref(1)

onMounted(async () => {
  loading.value = true
  try {
    rooms.value = await getHotelRooms(eventId)
  } catch {
    ElMessage.error('加载房型失败')
  } finally {
    loading.value = false
  }
})

async function onBook() {
  if (!selected.value) return ElMessage.warning('请选择房型')
  submitting.value = true
  try {
    const order = await createHotelBooking(eventId, { roomTypeId: selected.value, nights: nights.value })
    const mode = await launchPay(
      () => createPayOrder({
        bizType: 'HOTEL',
        bizId: order.id,
        channel: isMobilePayChannel() ? 'wap' : 'page',
      }),
      () => mockPay(order.id),
    )
    if (mode === 'mock') {
      ElMessage.success('预订并支付成功')
      router.push('/portal/orders')
    } else if (mode === 'alipay') {
      ElMessage.success('正在跳转支付宝…')
    }
  } catch {
    return
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <PortalBackBar />
    <div class="page-header">
      <h1>预订酒店</h1>
      <p>理事会成员与付费会员可预订协议酒店</p>
    </div>
    <el-empty v-if="!loading && !rooms.length" description="暂无可订房型，请确认企业端已配置酒店并开启活动酒店功能" />
    <el-radio-group v-else v-model="selected" class="room-list">
      <el-card v-for="room in rooms" :key="room.id" shadow="never" class="room-card" :class="{ selected: selected === room.id }">
        <el-radio :value="room.id">
          <span class="room-name">{{ room.name }}</span>
          <span class="room-price">¥{{ room.price }}/晚</span>
          <span class="room-quota">剩余 {{ room.quota }} 间</span>
        </el-radio>
      </el-card>
    </el-radio-group>
    <el-form v-if="rooms.length" inline style="margin-top: 1rem">
      <el-form-item label="入住晚数">
        <el-input-number v-model="nights" :min="1" :max="7" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="onBook">确认预订</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.room-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  max-width: 480px;
}

.room-card {
  cursor: pointer;
}

.room-card.selected {
  border-color: var(--primary);
}

.room-name {
  font-weight: 600;
  margin-right: 1rem;
}

.room-price {
  color: var(--primary);
  margin-right: 1rem;
}

.room-quota {
  font-size: 0.8125rem;
  color: var(--muted);
}
</style>
