<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export interface NavChild {
  path: string
  label: string
  disabled?: boolean
  exact?: boolean
  isActive?: (path: string) => boolean
}

export interface NavItem {
  path: string
  label: string
  children?: NavChild[]
}

const props = defineProps<{
  title: string
  nav: NavItem[]
}>()

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)
const expanded = ref(new Set<string>())

function isParentActive(item: NavItem) {
  return active.value === item.path || active.value.startsWith(`${item.path}/`)
}

function isChildActive(child: NavChild) {
  if (child.isActive) return child.isActive(active.value)
  if (child.exact) return active.value === child.path
  return active.value === child.path || active.value.startsWith(`${child.path}/`)
}

function parentHasActiveChild(item: NavItem) {
  return item.children?.some((c) => isChildActive(c)) ?? false
}

function isExpanded(item: NavItem) {
  return expanded.value.has(item.path)
}

function syncExpanded() {
  props.nav.forEach((item) => {
    if (item.children?.length && isParentActive(item)) {
      expanded.value.add(item.path)
    }
  })
}

watch(() => route.path, syncExpanded, { immediate: true })
watch(() => props.nav, syncExpanded, { deep: true })

function onParentClick(item: NavItem) {
  if (!isParentActive(item)) {
    expanded.value.add(item.path)
    const defaultChild = item.children?.find((c) => !c.disabled) ?? item.children?.[0]
    if (defaultChild && !defaultChild.disabled) {
      router.push(defaultChild.path)
    }
    return
  }
  if (isExpanded(item)) expanded.value.delete(item.path)
  else expanded.value.add(item.path)
}

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="logo-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 8.5v7M8 6v12M12 9v6M16 7v10M20 8.5v7" stroke="white" stroke-width="2" stroke-linecap="round" />
          </svg>
        </span>
        <span class="brand-name">HySaaS</span>
      </div>
      <p class="sidebar-title">{{ title }}</p>
      <nav class="nav">
        <template v-for="item in nav" :key="item.path">
          <button
            v-if="item.children?.length"
            type="button"
            class="nav-item nav-item-expand"
            :class="{
              active: isParentActive(item) && !parentHasActiveChild(item),
              'section-open': isParentActive(item) && parentHasActiveChild(item),
              expanded: isExpanded(item),
            }"
            @click="onParentClick(item)"
          >
            <span>{{ item.label }}</span>
            <svg class="chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 6l6 6-6 6" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
          <router-link
            v-else
            :to="item.path"
            class="nav-item"
            :class="{ active: isParentActive(item) }"
          >
            {{ item.label }}
          </router-link>
          <div v-if="item.children?.length && isExpanded(item)" class="nav-sub">
            <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.disabled ? route.path : child.path"
              class="nav-sub-item"
              :class="{ active: isChildActive(child), disabled: child.disabled }"
              @click="child.disabled && $event.preventDefault()"
            >
              {{ child.label }}
            </router-link>
          </div>
        </template>
      </nav>
    </aside>
    <div class="main-area">
      <header class="topbar">
        <h2 class="page-title">{{ (route.meta.title as string) || title }}</h2>
        <div class="user-area">
          <span class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-button text type="primary" @click="onLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100dvh;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border);
  padding: 1.25rem 0.75rem;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0 0.5rem 1.25rem;
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

.brand-name {
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: -0.02em;
}

.sidebar-title {
  font-size: 0.75rem;
  color: var(--muted);
  padding: 0 0.75rem;
  margin: 0 0 0.75rem;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: block;
  width: 100%;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--ink);
  text-decoration: none;
  transition: background 0.15s ease-out;
  border: none;
  background: none;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.nav-item-expand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.chevron {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: var(--muted);
  transition: transform 0.15s ease-out;
}

.nav-item.expanded .chevron {
  transform: rotate(90deg);
}

.nav-item:hover {
  background: var(--surface);
}

.nav-item.active,
.nav-item.section-open {
  background: oklch(0.52 0.14 230 / 0.1);
  color: var(--primary);
}

.nav-sub {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 2px 0 4px;
  padding-left: 0.625rem;
  border-left: 2px solid var(--border);
  margin-left: 0.75rem;
}

.nav-sub-item {
  display: block;
  padding: 0.4rem 0.625rem;
  border-radius: 6px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--muted);
  text-decoration: none;
  transition: background 0.15s ease-out, color 0.15s ease-out;
}

.nav-sub-item:hover:not(.disabled) {
  color: var(--ink);
  background: var(--surface);
}

.nav-sub-item.active {
  background: oklch(0.52 0.14 230 / 0.12);
  color: var(--primary);
  font-weight: 600;
}

.nav-sub-item.disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.page-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-name {
  font-size: 0.875rem;
  color: var(--muted);
}

.content {
  flex: 1;
  padding: 1.5rem;
  overflow: auto;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
