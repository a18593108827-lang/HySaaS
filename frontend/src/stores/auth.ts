import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, UserType } from '@/types'
import { getMe, login as loginApi, logout as logoutApi, type LoginParams } from '@/api/auth'

const HOME_MAP: Record<UserType, string> = {
  PLATFORM: '/admin/dashboard',
  ENTERPRISE: '/enterprise/dashboard',
  ATTENDEE: '/portal/events',
}

const TOKEN_KEY = 'token'

function readStoredToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(readStoredToken())
  const user = ref<UserInfo | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const userType = computed(() => user.value?.userType)
  const homePath = computed(() => (user.value ? HOME_MAP[user.value.userType] : '/login'))

  function setToken(t: string, remember = true) {
    token.value = t
    if (remember) {
      localStorage.setItem(TOKEN_KEY, t)
      sessionStorage.removeItem(TOKEN_KEY)
    } else {
      sessionStorage.setItem(TOKEN_KEY, t)
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(TOKEN_KEY)
  }

  async function login(params: LoginParams & { remember?: boolean }) {
    loading.value = true
    try {
      const res = await loginApi(params)
      setToken(res.token, params.remember !== false)
      await fetchUser()
      return res
    } finally {
      loading.value = false
    }
  }

  async function fetchUser() {
    if (!token.value) return null
    user.value = await getMe()
    return user.value
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  function hasRole(role: string) {
    return user.value?.roles.includes(role) ?? false
  }

  function hasAnyRole(roles: string[]) {
    return roles.some((r) => hasRole(r))
  }

  function hasEventPerm(code: string) {
    return user.value?.eventPermissions.includes(code) ?? false
  }

  return {
    token,
    user,
    loading,
    isLoggedIn,
    userType,
    homePath,
    setToken,
    login,
    fetchUser,
    logout,
    clearAuth,
    hasRole,
    hasAnyRole,
    hasEventPerm,
  }
})
