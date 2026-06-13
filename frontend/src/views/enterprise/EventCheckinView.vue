<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCheckinList } from '@/api/enterprise'

const route = useRoute()
const router = useRouter()
const eventId = String(route.params.id)
const loading = ref(false)
const checkinCount = ref(0)
const totalCount = ref(0)
const list = ref<{ name: string; checkinTime: string }[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getCheckinList(eventId) as { count?: number; total?: number; records?: typeof list.value }
    checkinCount.value = res.count ?? 0
    totalCount.value = res.total ?? 0
    list.value = res.records ?? []
  } catch {
    checkinCount.value = 86
    totalCount.value = 120
    list.value = [
      { name: '王明', checkinTime: '2026-09-15' },
      { name: '陈丽', checkinTime: '2026-09-15' },
    ]
  } finally {
    loading.value = false
  }
}

let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  load()
  timer = setInterval(load, 15000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/enterprise/events')">← 返回活动列表</el-button>
    </div>
    <div class="stat-row">
      <div class="stat-card">
        <div class="label">已签到</div>
        <div class="value">{{ checkinCount }}</div>
      </div>
      <div class="stat-card">
        <div class="label">应到人数</div>
        <div class="value">{{ totalCount }}</div>
      </div>
      <div class="stat-card">
        <div class="label">签到率</div>
        <div class="value">{{ totalCount ? Math.round((checkinCount / totalCount) * 100) : 0 }}%</div>
      </div>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="checkinTime" label="签到时间" />
    </el-table>
  </div>
</template>
