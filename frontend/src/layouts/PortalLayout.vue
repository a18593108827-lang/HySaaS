<script setup lang="ts">
import AppShell from './AppShell.vue'

const nav = [
  { path: '/portal/events', label: '活动' },
  { path: '/portal/submissions', label: '我的投稿' },
  { path: '/portal/orders', label: '我的订单' },
]
</script>

<template>
  <AppShell title="参会门户" :nav="nav" />
</template>
