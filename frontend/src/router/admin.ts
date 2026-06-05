import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { title: '平台概览' },
  },
  {
    path: 'tenants',
    name: 'AdminTenants',
    component: () => import('@/views/admin/TenantsView.vue'),
    meta: { title: '租户管理' },
  },
  {
    path: 'tenants/:id',
    name: 'AdminTenantDetail',
    component: () => import('@/views/admin/TenantDetailView.vue'),
    meta: { title: '租户详情' },
  },
  {
    path: 'users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UsersView.vue'),
    meta: { title: '用户管理' },
  },
  {
    path: 'users/:id',
    name: 'AdminUserDetail',
    component: () => import('@/views/admin/UserDetailView.vue'),
    meta: { title: '用户详情' },
  },
  {
    path: 'config',
    name: 'AdminConfig',
    component: () => import('@/views/admin/ConfigView.vue'),
    meta: { title: '全局配置' },
  },
]

export default adminRoutes
