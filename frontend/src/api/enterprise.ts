import request from './request'
import type { CheckinQrcodeResult, EnterpriseAttendee, EnterpriseAttendeePayload, EnterpriseMember, EnterpriseMemberPayload, EventInviteLinkResult, EventInvitePayload, EventInviteResult, EventItem, HotelBooking, HotelInfo, HotelInfoPayload, HotelRoomType, HotelRoomTypePayload, PageResult, PaperSubmission, PayOrder, Registration } from '@/types'

export function getEvents(params?: { page?: number; size?: number }) {
  return request.get<unknown, PageResult<EventItem>>('/enterprise/events', { params })
}

export function getEvent(id: number | string) {
  return request.get<unknown, EventItem>(`/enterprise/events/${id}`)
}

export function createEvent(data: Partial<EventItem>) {
  return request.post('/enterprise/events', data)
}

export function updateEvent(id: number | string, data: Partial<EventItem>) {
  return request.put(`/enterprise/events/${id}`, data)
}

export function publishEvent(id: number | string) {
  return request.post(`/enterprise/events/${id}/publish`)
}

export function deleteEvent(id: number | string) {
  return request.delete(`/enterprise/events/${id}`)
}

export function generateQrcode(id: number | string) {
  return request.post<unknown, CheckinQrcodeResult>(`/enterprise/events/${id}/qrcode`)
}

export function getRegistrations(eventId: number | string, params?: { status?: string }) {
  return request.get<unknown, PageResult<Registration>>(`/enterprise/events/${eventId}/registrations`, { params })
}

export function auditRegistration(id: number | string, status: 'APPROVED' | 'REJECTED') {
  return request.put(`/enterprise/registrations/${id}`, { status })
}

export function getCheckinList(eventId: number | string) {
  return request.get(`/enterprise/events/${eventId}/checkin`)
}

export function getPapers(params?: { status?: string }) {
  return request.get<unknown, PageResult<PaperSubmission>>('/enterprise/papers', { params })
}

export function assignReviewer(paperId: number, expertId: number) {
  return request.post(`/enterprise/papers/${paperId}/assign`, { expertId })
}

export function finalizePaper(paperId: number, status: 'ACCEPTED' | 'REJECTED' | 'REVISION') {
  return request.put(`/enterprise/papers/${paperId}/finalize`, { status })
}

export function getReviewTasks() {
  return request.get<unknown, { paperId: number; title: string; author: string; deadline: string }[]>('/enterprise/reviews')
}

export function submitReview(paperId: number, data: { comment: string; suggest: string }) {
  return request.post(`/enterprise/reviews/${paperId}`, data)
}

export function getHotels() {
  return request.get<unknown, HotelInfo[]>('/enterprise/hotels')
}

export function getHotel(id: number) {
  return request.get<unknown, HotelInfo>(`/enterprise/hotels/${id}`)
}

export function createHotel(data: HotelInfoPayload) {
  return request.post('/enterprise/hotels', data)
}

export function updateHotel(id: number, data: Partial<HotelInfoPayload>) {
  return request.put(`/enterprise/hotels/${id}`, data)
}

export function deleteHotel(id: number) {
  return request.delete(`/enterprise/hotels/${id}`)
}

export function getHotelRoomTypes(hotelId: number) {
  return request.get<unknown, HotelRoomType[]>(`/enterprise/hotels/${hotelId}/room-types`)
}

export function createHotelRoomType(hotelId: number, data: HotelRoomTypePayload) {
  return request.post(`/enterprise/hotels/${hotelId}/room-types`, data)
}

export function updateHotelRoomType(hotelId: number, id: number, data: Partial<HotelRoomTypePayload>) {
  return request.put(`/enterprise/hotels/${hotelId}/room-types/${id}`, data)
}

export function deleteHotelRoomType(hotelId: number, id: number) {
  return request.delete(`/enterprise/hotels/${hotelId}/room-types/${id}`)
}

export function getEmailTemplates(params?: { eventId?: number }) {
  return request.get<unknown, { id: number; code: string; name: string; subject: string; content: string; eventId?: number }[]>('/enterprise/email-templates', { params })
}

export function updateEmailTemplate(id: number, data: { subject?: string; content?: string }) {
  return request.put(`/enterprise/email-templates/${id}`, data)
}

export function getEmailLogs(params?: { page?: number; size?: number }) {
  return request.get<unknown, { records: { id: number; code: string; recipient: string; subject: string; status: string; errorMsg?: string; retryCount: number; createdAt: string }[]; total: number }>('/enterprise/email-templates/logs', { params })
}

export function getOrders(params?: { page?: number }) {
  return request.get<unknown, PageResult<PayOrder>>('/enterprise/finance/orders', { params })
}

export function getInvoices(params?: { page?: number }) {
  return request.get<unknown, { id: number; orderNo: string; title: string; amount: number; status: string; createdAt: string }[]>('/enterprise/finance/invoices', { params })
}

export function getBookings(params?: { status?: string; eventId?: number; page?: number; size?: number }) {
  return request.get<unknown, PageResult<HotelBooking>>('/enterprise/bookings', { params })
}

export function verifyBooking(id: number) {
  return request.post(`/enterprise/bookings/${id}/checkin`)
}

export function getMembers(params?: { role?: string; page?: number; size?: number }) {
  return request.get<unknown, PageResult<EnterpriseMember>>('/enterprise/members', { params })
}

export function getMember(id: number | string) {
  return request.get<unknown, EnterpriseMember>(`/enterprise/members/${id}`)
}

export function createMember(data: EnterpriseMemberPayload) {
  return request.post('/enterprise/members', data)
}

export function updateMember(id: number | string, data: EnterpriseMemberPayload) {
  return request.put<unknown, EnterpriseMember>(`/enterprise/members/${id}`, data)
}

export function deleteMember(id: number | string) {
  return request.delete(`/enterprise/members/${id}`)
}

export function getAttendees(params?: { nickname?: string; page?: number; size?: number }) {
  return request.get<unknown, PageResult<EnterpriseAttendee>>('/enterprise/attendees', { params })
}

export function inviteAttendees(eventId: number | string, data: EventInvitePayload) {
  return request.post<unknown, EventInviteResult>(`/enterprise/events/${eventId}/invites`, data)
}

export function generateInviteLink(eventId: number | string) {
  return request.post<unknown, EventInviteLinkResult>(`/enterprise/events/${eventId}/invite-link`)
}

export function getAttendee(id: number | string) {
  return request.get<unknown, EnterpriseAttendee>(`/enterprise/attendees/${id}`)
}

export function createAttendee(data: EnterpriseAttendeePayload) {
  return request.post('/enterprise/attendees', data)
}

export function updateAttendee(id: number | string, data: EnterpriseAttendeePayload) {
  return request.put<unknown, EnterpriseAttendee>(`/enterprise/attendees/${id}`, data)
}

export function deleteAttendee(id: number | string) {
  return request.delete(`/enterprise/attendees/${id}`)
}
