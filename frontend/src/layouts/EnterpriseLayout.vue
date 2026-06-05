<script setup lang="ts">
import { computed } from 'vue'
import AppShell from './AppShell.vue'

const nav = computed(() => [
  { path: '/enterprise/dashboard', label: '工作台' },
  { path: '/enterprise/members', label: '成员管理' },
  { path: '/enterprise/attendees', label: '参会账号' },
  { path: '/enterprise/events', label: '活动管理' },
  { path: '/enterprise/papers', label: '稿件管理' },
  { path: '/enterprise/reviews', label: '评审工作台' },
  { path: '/enterprise/hotels', label: '酒店协议' },
  { path: '/enterprise/email-templates', label: '邮件模板' },
  { path: '/enterprise/finance/orders', label: '订单' },
  { path: '/enterprise/finance/invoices', label: '发票' },
])
</script>

<template>
  <AppShell title="企业会务" :nav="nav" />
</template>
