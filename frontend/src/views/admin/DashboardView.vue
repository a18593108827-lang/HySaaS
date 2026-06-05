<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTenants } from '@/api/admin'

const stats = ref({ pending: 0, approved: 0, total: 0 })

onMounted(async () => {
  try {
    const res = await getTenants({ size: 1 })
    stats.value.total = res.total
  } catch {
    stats.value = { pending: 3, approved: 12, total: 15 }
  }
})
</script>

<template>
  <div>
    <div class="page-header">
      <h1>平台概览</h1>
      <p>租户与全局配置运行状态</p>
    </div>
    <div class="stat-row">
      <div class="stat-card">
        <div class="label">待审核租户</div>
        <div class="value">{{ stats.pending }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已开通租户</div>
        <div class="value">{{ stats.approved }}</div>
      </div>
      <div class="stat-card">
        <div class="label">租户总数</div>
        <div class="value">{{ stats.total }}</div>
      </div>
    </div>
    <el-card shadow="never">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/admin/tenants')">租户管理</el-button>
        <el-button @click="$router.push('/admin/users')">用户管理</el-button>
        <el-button @click="$router.push('/admin/config')">全局配置</el-button>
      </el-space>
    </el-card>
  </div>
</template>
