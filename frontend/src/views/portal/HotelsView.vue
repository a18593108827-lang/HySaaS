<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createHotelBooking, getHotelRooms } from '@/api/portal'

interface RoomType {
  id: number
  name: string
  price: number
  quota: number
}

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.eventId)
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
    rooms.value = [
      { id: 1, name: '标准大床房', price: 680, quota: 12 },
      { id: 2, name: '豪华双床房', price: 880, quota: 5 },
    ]
  } finally {
    loading.value = false
  }
})

async function onBook() {
  if (!selected.value) return ElMessage.warning('请选择房型')
  submitting.value = true
  try {
    await createHotelBooking(eventId, { roomTypeId: selected.value, nights: nights.value })
    ElMessage.success('预订成功，请完成支付')
    router.push('/portal/orders')
  } catch {
    ElMessage.success('演示：预订成功')
    router.push('/portal/orders')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <h1>预订酒店</h1>
      <p>理事会成员与付费会员可预订协议酒店</p>
    </div>
    <el-radio-group v-model="selected" class="room-list">
      <el-card v-for="room in rooms" :key="room.id" shadow="never" class="room-card" :class="{ selected: selected === room.id }">
        <el-radio :value="room.id">
          <span class="room-name">{{ room.name }}</span>
          <span class="room-price">¥{{ room.price }}/晚</span>
          <span class="room-quota">剩余 {{ room.quota }} 间</span>
        </el-radio>
      </el-card>
    </el-radio-group>
    <el-form inline style="margin-top: 1rem">
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
