import type { RouteRecordRaw } from 'vue-router'
import { ENTERPRISE_NAV_ROLES } from '@/constants/enterpriseRoles'

const eventOps = ENTERPRISE_NAV_ROLES['/enterprise/events']

const enterpriseRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'EnterpriseDashboard',
    component: () => import('@/views/enterprise/DashboardView.vue'),
    meta: { title: '工作台', roles: ENTERPRISE_NAV_ROLES['/enterprise/dashboard'] },
  },
  {
    path: 'members',
    name: 'EnterpriseMembers',
    component: () => import('@/views/enterprise/MembersView.vue'),
    meta: { title: '成员管理', roles: ENTERPRISE_NAV_ROLES['/enterprise/members'] },
  },
  {
    path: 'members/:id',
    name: 'EnterpriseMemberDetail',
    component: () => import('@/views/enterprise/MemberDetailView.vue'),
    meta: { title: '成员详情', roles: ENTERPRISE_NAV_ROLES['/enterprise/members'] },
  },
  {
    path: 'attendees',
    name: 'EnterpriseAttendees',
    component: () => import('@/views/enterprise/AttendeesView.vue'),
    meta: { title: '参会账号', roles: ENTERPRISE_NAV_ROLES['/enterprise/attendees'] },
  },
  {
    path: 'attendees/:id',
    name: 'EnterpriseAttendeeDetail',
    component: () => import('@/views/enterprise/AttendeeDetailView.vue'),
    meta: { title: '参会账号详情', roles: ENTERPRISE_NAV_ROLES['/enterprise/attendees'] },
  },
  {
    path: 'events',
    name: 'EnterpriseEvents',
    component: () => import('@/views/enterprise/EventsView.vue'),
    meta: { title: '活动管理', roles: eventOps },
  },
  {
    path: 'events/:id',
    component: () => import('@/views/enterprise/EventDetailLayout.vue'),
    meta: { title: '活动详情', roles: eventOps },
    children: [
      { path: '', redirect: (to) => `/enterprise/events/${to.params.id}/registrations` },
      {
        path: 'registrations',
        name: 'EventRegistrations',
        component: () => import('@/views/enterprise/EventRegistrationsView.vue'),
        meta: { title: '报名审核', roles: eventOps },
      },
      {
        path: 'checkin',
        name: 'EventCheckin',
        component: () => import('@/views/enterprise/EventCheckinView.vue'),
        meta: { title: '签到管理', roles: eventOps },
      },
    ],
  },
  {
    path: 'papers',
    name: 'EnterprisePapers',
    component: () => import('@/views/enterprise/PapersView.vue'),
    meta: { title: '稿件管理', roles: ENTERPRISE_NAV_ROLES['/enterprise/papers'] },
  },
  {
    path: 'reviews',
    name: 'EnterpriseReviews',
    component: () => import('@/views/enterprise/ReviewsView.vue'),
    meta: { title: '评审工作台', roles: ENTERPRISE_NAV_ROLES['/enterprise/reviews'] },
  },
  {
    path: 'hotels',
    name: 'EnterpriseHotels',
    component: () => import('@/views/enterprise/HotelsView.vue'),
    meta: { title: '酒店协议', roles: ENTERPRISE_NAV_ROLES['/enterprise/hotels'] },
  },
  {
    path: 'bookings',
    name: 'EnterpriseBookings',
    component: () => import('@/views/enterprise/BookingsView.vue'),
    meta: { title: '房单核销', roles: ENTERPRISE_NAV_ROLES['/enterprise/bookings'] },
  },
  {
    path: 'email-templates',
    name: 'EmailTemplates',
    component: () => import('@/views/enterprise/EmailTemplatesView.vue'),
    meta: { title: '邮件模板', roles: ENTERPRISE_NAV_ROLES['/enterprise/email-templates'] },
  },
  {
    path: 'finance/orders',
    name: 'FinanceOrders',
    component: () => import('@/views/enterprise/FinanceOrdersView.vue'),
    meta: { title: '订单管理', roles: ENTERPRISE_NAV_ROLES['/enterprise/finance/orders'] },
  },
  {
    path: 'finance/invoices',
    name: 'FinanceInvoices',
    component: () => import('@/views/enterprise/FinanceInvoicesView.vue'),
    meta: { title: '发票管理', roles: ENTERPRISE_NAV_ROLES['/enterprise/finance/invoices'] },
  },
]

export default enterpriseRoutes
