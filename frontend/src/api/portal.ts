import request from './request'
import type { EventItem, PageResult, PaperSubmission, PayOrder } from '@/types'

export function getPortalEvents() {
  return request.get<unknown, EventItem[]>('/portal/events')
}

export function getPortalEvent(id: number, token?: string) {
  return request.get<unknown, EventItem>(`/portal/events/${id}`, { params: { token } })
}

export function submitRegistration(eventId: number, data: Record<string, string>) {
  return request.post(`/portal/events/${eventId}/register`, data)
}

export function getMySubmissions() {
  return request.get<unknown, PaperSubmission[]>('/portal/submissions')
}

export function saveDraft(data: Partial<PaperSubmission>) {
  return request.post('/portal/submissions/draft', data)
}

export function submitPaper(id: number) {
  return request.post(`/portal/submissions/${id}/submit`)
}

export function checkin(eventId: number, token?: string) {
  return request.post(`/portal/checkin/${eventId}`, token ? { token } : undefined)
}

export function getHotelRooms(eventId: number) {
  return request.get<unknown, { id: number; name: string; price: number; quota: number }[]>(`/portal/hotels/${eventId}`)
}

export function createHotelBooking(eventId: number, data: Record<string, unknown>) {
  return request.post(`/portal/hotels/${eventId}/book`, data)
}

export function getMyOrders(params?: { page?: number }) {
  return request.get<unknown, PageResult<PayOrder>>('/portal/orders', { params })
}

export function createPayOrder(data: { bizType: string; bizId: number }) {
  return request.post<unknown, { payUrl: string }>('/portal/pay/create', data)
}

export function applyInvoice(data: Record<string, string>) {
  return request.post('/portal/invoices/apply', data)
}
