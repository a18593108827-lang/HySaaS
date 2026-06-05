<script setup lang="ts">
import AppShell from './AppShell.vue'

const nav = [
  { path: '/admin/dashboard', label: '概览' },
  { path: '/admin/tenants', label: '租户管理' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/admin/config', label: '全局配置' },
]
</script>

<template>
  <AppShell title="平台管理" :nav="nav" />
</template>
