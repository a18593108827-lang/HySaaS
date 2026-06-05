<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getEvents } from '@/api/enterprise'

const stats = ref({ events: 0, registrations: 0, papers: 0 })

onMounted(async () => {
  try {
    const res = await getEvents({ size: 1 })
    stats.value.events = res.total
  } catch {
    stats.value = { events: 5, registrations: 128, papers: 42 }
  }
})
</script>

<template>
  <div>
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
        <div class="value">{{ stats.registrations }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待处理稿件</div>
        <div class="value">{{ stats.papers }}</div>
      </div>
    </div>
    <el-card shadow="never">
      <template #header>常用操作</template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/enterprise/events')">管理活动</el-button>
        <el-button @click="$router.push('/enterprise/members')">成员管理</el-button>
        <el-button @click="$router.push('/enterprise/papers')">处理稿件</el-button>
      </el-space>
    </el-card>
  </div>
</template>
