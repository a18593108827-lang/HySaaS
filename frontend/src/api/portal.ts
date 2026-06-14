import request from './request'
import type { PayCreateResult } from '@/utils/pay'
import type { EventItem, InvoiceApplyPayload, PageResult, PaperSubmission, PayOrder, PortalRegisterResult } from '@/types'

export function getPortalEvents() {
  return request.get<unknown, EventItem[]>('/portal/events')
}

export function getPortalEvent(id: number | string, token?: string) {
  return request.get<unknown, EventItem>(`/portal/events/${id}`, { params: { token } })
}

export function submitRegistration(eventId: number | string, data: Record<string, string>) {
  return request.post<unknown, PortalRegisterResult>(`/portal/events/${eventId}/register`, data)
}

export function getMySubmissions(scope: 'draft' | 'submitted' = 'draft') {
  return request.get<unknown, PaperSubmission[]>('/portal/submissions', { params: { scope } })
}

export function saveDraft(data: {
  id?: number | string
  title: string
  author?: string
  abstract?: string
}) {
  return request.post<unknown, PaperSubmission>('/portal/submissions/draft', data)
}

export function uploadPaperFile(id: number | string, file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<unknown, PaperSubmission>(`/portal/submissions/${id}/file`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function submitPaper(id: number | string, eventId?: number | string) {
  return request.post(`/portal/submissions/${id}/submit`, eventId != null ? { eventId } : undefined)
}

export function checkin(eventId: number | string, token?: string) {
  return request.post(`/portal/checkin/${eventId}`, token ? { token } : undefined)
}

export function getHotelRooms(eventId: number | string) {
  return request.get<unknown, { id: number; name: string; price: number; quota: number }[]>(`/portal/hotels/${eventId}`)
}

export function createHotelBooking(eventId: number | string, data: Record<string, unknown>) {
  return request.post<unknown, PayOrder>(`/portal/hotels/${eventId}/book`, data)
}

export function getMyOrders(params?: { page?: number }) {
  return request.get<unknown, PageResult<PayOrder>>('/portal/orders', { params })
}

export function createPayOrder(data: { bizType: string; bizId: number | string; channel?: string }) {
  return request.post<unknown, PayCreateResult>('/portal/pay/create', data)
}

export function mockPay(orderId: number | string) {
  return request.post(`/portal/pay/mock/${orderId}`)
}

export function applyInvoice(data: InvoiceApplyPayload) {
  return request.post('/portal/invoices/apply', data)
}
