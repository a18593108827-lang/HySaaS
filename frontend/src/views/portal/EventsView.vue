<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPortalEvents } from '@/api/portal'
import type { EventItem } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<EventItem[]>([])

const regStatusLabel: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
}

const hotelMemberTypes = new Set(['付费会员', '理事会成员'])

function canBookHotel(item: EventItem) {
  return item.myRegistrationStatus === 'APPROVED' && hotelMemberTypes.has(item.myMemberType || '')
}

async function load() {
  loading.value = true
  try {
    list.value = await getPortalEvents()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>活动列表</h1>
      <p>浏览可报名、可投稿的会议活动</p>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无可参与的活动" />
    <div v-else class="event-grid">
      <el-card v-for="item in list" :key="item.id" shadow="hover" class="event-card">
        <div class="card-head">
          <h3>{{ item.title }}</h3>
          <el-tag v-if="item.myRegistrationStatus" size="small" :type="item.myRegistrationStatus === 'APPROVED' ? 'success' : item.myRegistrationStatus === 'REJECTED' ? 'danger' : 'warning'">
            {{ regStatusLabel[item.myRegistrationStatus] }}
          </el-tag>
        </div>
        <p class="meta">{{ item.location }} · {{ item.startTime }} ~ {{ item.endTime }}</p>
        <div class="actions">
          <el-button v-if="item.registrationEnabled && !item.myRegistrationStatus" type="primary" @click="router.push(`/event/${item.id}/register`)">报名</el-button>
          <el-button v-if="item.paperEnabled" @click="router.push('/portal/submissions')">投稿</el-button>
          <el-tooltip v-if="item.hotelEnabled" :disabled="canBookHotel(item)" content="需报名审核通过，且为付费会员/理事会成员">
            <el-button :disabled="!canBookHotel(item)" @click="router.push(`/portal/hotels/${item.id}`)">订酒店</el-button>
          </el-tooltip>
          <el-tag v-if="item.myRegistrationStatus === 'APPROVED'" type="info" size="small">请现场扫码签到</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.event-card h3 {
  margin: 0;
  font-size: 1.125rem;
}

.meta {
  font-size: 0.875rem;
  color: var(--muted);
  margin: 0 0 1rem;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
