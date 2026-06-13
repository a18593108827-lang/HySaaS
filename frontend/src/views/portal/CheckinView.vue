<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { checkin, getPortalEvent } from '@/api/portal'
import PortalBackBar from '@/components/PortalBackBar.vue'
import type { EventItem } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const eventId = String(route.params.eventId)
const inPortal = computed(() => route.path.startsWith('/portal/'))
const scanToken = computed(() => (route.query.token as string) || '')
const fromScan = computed(() => !!scanToken.value)

const loading = ref(false)
const eventLoading = ref(true)
const done = ref(false)
const error = ref('')
const event = ref<EventItem | null>(null)

async function doCheckin() {
  if (loading.value || done.value) return
  loading.value = true
  error.value = ''
  try {
    await checkin(eventId, scanToken.value || undefined)
    done.value = true
    ElMessage.success('签到成功')
  } catch {
    error.value = '签到失败，请确认已报名并通过审核'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    event.value = await getPortalEvent(eventId, scanToken.value || undefined)
  } catch {
    if (route.path.startsWith('/portal/')) {
      ElMessage.error('加载活动信息失败')
      router.replace('/portal/events')
      return
    }
    error.value = '活动信息加载失败'
  } finally {
    eventLoading.value = false
  }

  if (!auth.token || !auth.user) {
    if (fromScan.value) {
      return router.replace({ path: '/login', query: { redirect: route.fullPath } })
    }
    return
  }

  if (auth.userType !== 'ATTENDEE') {
    error.value = '请使用参会人账号登录后签到'
    return
  }

  if (fromScan.value) {
    await doCheckin()
  }
})

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}
</script>

<template>
  <div v-if="inPortal" v-loading="eventLoading" class="checkin-portal">
    <PortalBackBar />
    <div class="page-header">
      <h1>现场签到</h1>
      <p v-if="event">{{ event.title }} · {{ event.location }} · {{ event.startTime }}</p>
    </div>
    <el-result v-if="done" icon="success" title="签到成功" sub-title="欢迎参会，请入场" />
    <div v-else-if="error" class="checkin-card error">
      <p>{{ error }}</p>
      <el-button v-if="!auth.user" type="primary" @click="goLogin">登录参会账号</el-button>
    </div>
    <div v-else-if="!auth.user" class="checkin-card">
      <p>登录后可签到</p>
      <el-button type="primary" @click="goLogin">登录签到</el-button>
    </div>
    <div v-else class="checkin-card">
      <p>到达会场后，点击下方按钮完成签到</p>
      <el-button type="primary" :loading="loading" @click="doCheckin">确认签到</el-button>
    </div>
  </div>

  <div v-else class="checkin-page">
    <header class="checkin-header">
      <span class="logo-mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 8.5v7M8 6v12M12 9v6M16 7v10M20 8.5v7" stroke="white" stroke-width="2" stroke-linecap="round" />
        </svg>
      </span>
      <span class="brand">HySaaS</span>
    </header>

    <main v-loading="eventLoading" class="checkin-main">
      <template v-if="event">
        <h1>{{ event.title }}</h1>
        <p class="meta">{{ event.location }} · {{ event.startTime }}</p>
      </template>

      <el-result v-if="done" icon="success" title="签到成功" sub-title="欢迎参会，请入场" />

      <div v-else-if="error" class="checkin-card error">
        <p>{{ error }}</p>
        <el-button v-if="!auth.user" type="primary" @click="goLogin">登录参会账号</el-button>
      </div>

      <div v-else-if="!auth.user" class="checkin-card">
        <p>{{ fromScan ? '请先登录参会账号以完成签到' : '登录后可签到' }}</p>
        <el-button type="primary" size="large" @click="goLogin">登录签到</el-button>
      </div>

      <div v-else class="checkin-card">
        <p>{{ fromScan ? '正在处理签到…' : '到达会场后，点击下方按钮完成签到' }}</p>
        <el-button type="primary" size="large" :loading="loading" @click="doCheckin">确认签到</el-button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.checkin-portal {
  max-width: 480px;
}

.checkin-page {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.checkin-header {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border);
}

.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--primary);
  display: grid;
  place-items: center;
}

.logo-mark svg {
  width: 18px;
  height: 18px;
}

.brand {
  font-weight: 700;
  letter-spacing: -0.02em;
}

.checkin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem;
  text-align: center;
}

.checkin-main h1 {
  margin: 0 0 0.5rem;
  font-size: 1.375rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  text-wrap: balance;
}

.meta {
  margin: 0 0 2rem;
  font-size: 0.875rem;
  color: var(--muted);
}

.checkin-card {
  width: 100%;
  max-width: 360px;
  padding: 2rem 1.5rem;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.checkin-card p {
  margin: 0 0 1.5rem;
  color: var(--muted);
  font-size: 0.9375rem;
}

.checkin-card.error p {
  color: var(--error);
}
</style>
