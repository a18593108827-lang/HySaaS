import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, UserType } from '@/types'
import { getMe, login as loginApi, logout as logoutApi, type LoginParams } from '@/api/auth'

const HOME_MAP: Record<UserType, string> = {
  PLATFORM: '/admin/dashboard',
  ENTERPRISE: '/enterprise/dashboard',
  ATTENDEE: '/portal/events',
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const userType = computed(() => user.value?.userType)
  const homePath = computed(() => (user.value ? HOME_MAP[user.value.userType] : '/login'))

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('demo-user-type')
  }

  async function login(params: LoginParams) {
    loading.value = true
    try {
      const res = await loginApi(params)
      setToken(res.token)
      await fetchUser()
      return res
    } catch {
      if (import.meta.env.DEV) {
        const userType: UserType = params.username.includes('admin')
          ? 'PLATFORM'
          : params.username.includes('ent')
            ? 'ENTERPRISE'
            : 'ATTENDEE'
        setToken('demo-token')
        localStorage.setItem('demo-user-type', userType)
        user.value = {
          id: 1,
          username: params.username,
          nickname: '演示用户',
          userType,
          roles: [],
          eventPermissions: [],
        }
        return { token: 'demo-token', userType }
      }
      throw new Error('账号或密码不正确')
    } finally {
      loading.value = false
    }
  }

  async function fetchUser() {
    if (!token.value) return null
    if (token.value === 'demo-token') {
      if (!user.value) {
        const saved = localStorage.getItem('demo-user-type') as UserType | null
        const userType = saved && HOME_MAP[saved] ? saved : 'ATTENDEE'
        user.value = {
          id: 1,
          username: 'demo',
          nickname: '演示用户',
          userType,
          roles: [],
          eventPermissions: [],
        }
      }
      return user.value
    }
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
