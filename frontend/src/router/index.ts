import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { UserType } from '@/types'
import adminRoutes from './admin'
import enterpriseRoutes from './enterprise'
import portalRoutes from './portal'

const PREFIX_TYPE: Record<string, UserType> = {
  '/admin': 'PLATFORM',
  '/enterprise': 'ENTERPRISE',
  '/portal': 'ATTENDEE',
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/register/attendee',
      name: 'AttendeeRegister',
      component: () => import('@/views/public/AttendeeRegisterView.vue'),
      meta: { public: true, title: '参会注册' },
    },
    {
      path: '/register',
      name: 'TenantRegister',
      component: () => import('@/views/public/TenantRegisterView.vue'),
      meta: { public: true, title: '企业入驻' },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { userType: 'PLATFORM' as UserType },
      children: [{ path: '', redirect: '/admin/dashboard' }, ...adminRoutes],
    },
    {
      path: '/enterprise',
      component: () => import('@/layouts/EnterpriseLayout.vue'),
      meta: { userType: 'ENTERPRISE' as UserType },
      children: [{ path: '', redirect: '/enterprise/dashboard' }, ...enterpriseRoutes],
    },
    {
      path: '/checkin/:eventId',
      name: 'ScanCheckin',
      component: () => import('@/views/portal/CheckinView.vue'),
      meta: { title: '现场签到', requiresAttendee: true },
    },
    {
      path: '/portal',
      component: () => import('@/layouts/PortalLayout.vue'),
      meta: { userType: 'ATTENDEE' as UserType },
      children: [{ path: '', redirect: '/portal/events' }, ...portalRoutes],
    },
    { path: '/', redirect: '/login' },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    try {
      await auth.fetchUser()
    } catch {
      auth.clearAuth()
    }
  }

  if (to.meta.public) {
    if (auth.user && (to.path === '/login' || to.path === '/register')) {
      return auth.homePath
    }
    return true
  }

  if (to.meta.requiresAttendee) {
    if (!auth.token || !auth.user) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (auth.userType !== 'ATTENDEE') {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    return true
  }

  if (!auth.token || !auth.user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const prefix = Object.keys(PREFIX_TYPE).find((p) => to.path.startsWith(p))
  if (prefix && auth.userType !== PREFIX_TYPE[prefix]) {
    return auth.homePath
  }

  const roles = to.meta.roles as string[] | undefined
  if (roles?.length && !roles.some((r) => auth.hasRole(r))) {
    return auth.homePath
  }

  const eventPerm = to.meta.eventPerm as string | undefined
  if (eventPerm && !auth.hasEventPerm(eventPerm)) {
    return auth.homePath
  }

  return true
})

export default router
