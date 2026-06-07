import type { RouteRecordRaw } from 'vue-router'

const enterpriseRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'EnterpriseDashboard',
    component: () => import('@/views/enterprise/DashboardView.vue'),
    meta: { title: '工作台' },
  },
  {
    path: 'members',
    name: 'EnterpriseMembers',
    component: () => import('@/views/enterprise/MembersView.vue'),
    meta: { title: '成员管理' },
  },
  {
    path: 'members/:id',
    name: 'EnterpriseMemberDetail',
    component: () => import('@/views/enterprise/MemberDetailView.vue'),
    meta: { title: '成员详情' },
  },
  {
    path: 'attendees',
    name: 'EnterpriseAttendees',
    component: () => import('@/views/enterprise/AttendeesView.vue'),
    meta: { title: '参会账号' },
  },
  {
    path: 'attendees/:id',
    name: 'EnterpriseAttendeeDetail',
    component: () => import('@/views/enterprise/AttendeeDetailView.vue'),
    meta: { title: '参会账号详情' },
  },
  {
    path: 'events',
    name: 'EnterpriseEvents',
    component: () => import('@/views/enterprise/EventsView.vue'),
    meta: { title: '活动管理' },
  },
  {
    path: 'events/:id',
    component: () => import('@/views/enterprise/EventDetailLayout.vue'),
    meta: { title: '活动详情' },
    children: [
      { path: '', redirect: (to) => `/enterprise/events/${to.params.id}/registrations` },
      {
        path: 'registrations',
        name: 'EventRegistrations',
        component: () => import('@/views/enterprise/EventRegistrationsView.vue'),
        meta: { title: '报名审核' },
      },
      {
        path: 'checkin',
        name: 'EventCheckin',
        component: () => import('@/views/enterprise/EventCheckinView.vue'),
        meta: { title: '签到管理' },
      },
    ],
  },
  {
    path: 'papers',
    name: 'EnterprisePapers',
    component: () => import('@/views/enterprise/PapersView.vue'),
    meta: { title: '稿件管理' },
  },
  {
    path: 'reviews',
    name: 'EnterpriseReviews',
    component: () => import('@/views/enterprise/ReviewsView.vue'),
    meta: { title: '评审工作台' },
  },
  {
    path: 'hotels',
    name: 'EnterpriseHotels',
    component: () => import('@/views/enterprise/HotelsView.vue'),
    meta: { title: '酒店协议' },
  },
  {
    path: 'bookings',
    name: 'EnterpriseBookings',
    component: () => import('@/views/enterprise/BookingsView.vue'),
    meta: { title: '房单核销' },
  },
  {
    path: 'email-templates',
    name: 'EmailTemplates',
    component: () => import('@/views/enterprise/EmailTemplatesView.vue'),
    meta: { title: '邮件模板' },
  },
  {
    path: 'finance/orders',
    name: 'FinanceOrders',
    component: () => import('@/views/enterprise/FinanceOrdersView.vue'),
    meta: { title: '订单管理' },
  },
  {
    path: 'finance/invoices',
    name: 'FinanceInvoices',
    component: () => import('@/views/enterprise/FinanceInvoicesView.vue'),
    meta: { title: '发票管理' },
  },
]

export default enterpriseRoutes
