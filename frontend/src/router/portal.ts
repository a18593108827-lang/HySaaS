import type { RouteRecordRaw } from 'vue-router'

const portalRoutes: RouteRecordRaw[] = [
  {
    path: 'events',
    name: 'PortalEvents',
    component: () => import('@/views/portal/EventsView.vue'),
    meta: { title: '活动列表' },
  },
  {
    path: 'events/:id/register',
    redirect: (to) => ({ path: `/event/${to.params.id}/register`, query: to.query }),
  },
  {
    path: 'events/:eventId/submissions',
    redirect: '/portal/submissions',
  },
  {
    path: 'submissions',
    name: 'PortalSubmissions',
    component: () => import('@/views/portal/SubmissionsView.vue'),
    meta: { title: '我的投稿' },
  },
  {
    path: 'orders',
    name: 'PortalOrders',
    component: () => import('@/views/portal/OrdersView.vue'),
    meta: { title: '我的订单' },
  },
  {
    path: 'checkin/:eventId',
    name: 'PortalCheckin',
    component: () => import('@/views/portal/CheckinView.vue'),
    meta: { title: '现场签到', requiresAttendee: true },
  },
  {
    path: 'hotels/:eventId',
    name: 'PortalHotels',
    component: () => import('@/views/portal/HotelsView.vue'),
    meta: { title: '预订酒店' },
  },
]

export default portalRoutes
