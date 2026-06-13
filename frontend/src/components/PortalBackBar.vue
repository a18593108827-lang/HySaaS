<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="toolbar">
    <el-button link type="primary" @click="router.push('/portal/events')">← 返回活动列表</el-button>
  </div>
</template>
