<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPortalEvents } from '@/api/portal'
import type { EventItem } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<EventItem[]>([])

async function load() {
  loading.value = true
  try {
    list.value = await getPortalEvents()
  } catch {
    list.value = [
      { id: 1, title: '2026 医学年会', location: '上海', startTime: '2026-09-15', endTime: '2026-09-17', status: 'REGISTRATION_OPEN', registrationEnabled: true, paperEnabled: true, hotelEnabled: true },
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
      <h1>活动列表</h1>
      <p>浏览可报名、可投稿的会议活动</p>
    </div>
    <el-empty v-if="!loading && !list.length" description="暂无可参与的活动" />
    <div v-else class="event-grid">
      <el-card v-for="item in list" :key="item.id" shadow="hover" class="event-card">
        <h3>{{ item.title }}</h3>
        <p class="meta">{{ item.location }} · {{ item.startTime }} ~ {{ item.endTime }}</p>
        <div class="actions">
          <el-button v-if="item.registrationEnabled" type="primary" @click="router.push(`/portal/events/${item.id}/register`)">报名</el-button>
          <el-button v-if="item.paperEnabled" @click="router.push('/portal/submissions')">投稿</el-button>
          <el-button v-if="item.hotelEnabled" @click="router.push(`/portal/hotels/${item.id}`)">订酒店</el-button>
          <el-button @click="router.push(`/portal/checkin/${item.id}`)">签到</el-button>
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

.event-card h3 {
  margin: 0 0 0.5rem;
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
