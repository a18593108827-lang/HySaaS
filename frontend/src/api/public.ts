import request from './request'
import type { LoginResult, TenantApplyPayload } from '@/types'

export function applyTenant(data: TenantApplyPayload) {
  return request.post('/public/tenant/apply', data)
}

export interface AttendeeRegisterPayload {
  eventId: number | string
  nickname: string
  email: string
  phone: string
  password: string
}

export function registerAttendee(data: AttendeeRegisterPayload) {
  return request.post<unknown, LoginResult>('/public/attendee/register', data)
}
