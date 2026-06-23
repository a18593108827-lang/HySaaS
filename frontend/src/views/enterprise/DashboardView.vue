<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/enterprise'
import { ENTERPRISE_NAV_ROLES } from '@/constants/enterpriseRoles'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const stats = ref({ events: 0, pendingRegistrations: 0, pendingPapers: 0 })

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } catch {
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <h1>工作台</h1>
      <p>当前租户会务数据概览</p>
    </div>
    <div class="stat-row">
      <div class="stat-card">
        <div class="label">进行中活动</div>
        <div class="value">{{ stats.events }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待审核报名</div>
        <div class="value">{{ stats.pendingRegistrations }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待处理稿件</div>
        <div class="value">{{ stats.pendingPapers }}</div>
      </div>
    </div>
    <el-card shadow="never">
      <template #header>常用操作</template>
      <el-space wrap>
        <el-button v-if="auth.hasAnyRole(ENTERPRISE_NAV_ROLES['/enterprise/events'])" type="primary" @click="$router.push('/enterprise/events')">管理活动</el-button>
        <el-button v-if="auth.hasAnyRole(ENTERPRISE_NAV_ROLES['/enterprise/members'])" @click="$router.push('/enterprise/members')">成员管理</el-button>
        <el-button v-if="auth.hasAnyRole(ENTERPRISE_NAV_ROLES['/enterprise/papers'])" @click="$router.push('/enterprise/papers')">处理稿件</el-button>
      </el-space>
    </el-card>
  </div>
</template>
