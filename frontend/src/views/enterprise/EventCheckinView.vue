<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCheckinList, getRegistrations, proxyCheckin } from '@/api/enterprise'
import type { Registration } from '@/types'

const route = useRoute()
const router = useRouter()
const eventId = String(route.params.id)
const loading = ref(false)
const pendingLoading = ref(false)
const keyword = ref('')
const activeTab = ref('checked')
const checkinCount = ref(0)
const totalCount = ref(0)
const list = ref<{ userId: number; name: string; source: string; checkinTime: string }[]>([])
const pending = ref<Registration[]>([])
const proxyingId = ref<number | null>(null)

const checkedUserIds = computed(() => new Set(list.value.map((r) => r.userId)))

const filteredPending = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return pending.value
  return pending.value.filter((r) =>
    r.name.toLowerCase().includes(q)
    || (r.email || '').toLowerCase().includes(q)
    || (r.phone || '').includes(q),
  )
})

const sourceLabel: Record<string, string> = {
  SCAN: '扫码',
  PROXY: '代签',
}

async function load() {
  loading.value = true
  try {
    const res = await getCheckinList(eventId)
    checkinCount.value = res.count ?? 0
    totalCount.value = res.total ?? 0
    list.value = res.records ?? []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadPending() {
  pendingLoading.value = true
  try {
    const res = await getRegistrations(eventId, { status: 'APPROVED', size: 500 })
    pending.value = res.records.filter((r) => r.userId && !checkedUserIds.value.has(r.userId))
  } catch {
    pending.value = []
  } finally {
    pendingLoading.value = false
  }
}

async function refreshAll() {
  await load()
  await loadPending()
}

async function handleProxy(row: Registration) {
  proxyingId.value = row.id
  try {
    await proxyCheckin(eventId, row.id)
    ElMessage.success(`已为「${row.name}」代签`)
    await refreshAll()
  } catch {
    return
  } finally {
    proxyingId.value = null
  }
}

let timer: ReturnType<typeof setInterval> | undefined
watch(activeTab, (tab) => {
  if (tab === 'pending') loadPending()
})
onMounted(() => {
  refreshAll()
  timer = setInterval(load, 15000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button link type="primary" @click="router.push('/enterprise/events')">← 返回活动列表</el-button>
      <el-button @click="refreshAll">刷新</el-button>
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

    <el-tabs v-model="activeTab">
      <el-tab-pane label="已签到" name="checked">
        <el-table v-loading="loading" :data="list" border stripe>
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="source" label="方式" width="90">
            <template #default="{ row }">{{ sourceLabel[row.source] || row.source }}</template>
          </el-table-column>
          <el-table-column prop="checkinTime" label="签到时间" width="170" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="代签" name="pending">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索姓名/邮箱/手机" clearable style="max-width: 260px" />
        </div>
        <el-table v-loading="pendingLoading" :data="filteredPending" border stripe>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" min-width="160" />
          <el-table-column prop="phone" label="手机" width="130" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :loading="proxyingId === row.id" @click="handleProxy(row)">代签</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
