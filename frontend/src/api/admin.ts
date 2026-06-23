import request from './request'
import type { AdminUser, AdminUserPayload, PageResult, Tenant } from '@/types'

export function getTenants(params?: { status?: string; page?: number; size?: number }) {
  return request.get<unknown, PageResult<Tenant>>('/admin/tenants', { params })
}

export function getTenant(id: number | string) {
  return request.get<unknown, Tenant>(`/admin/tenants/${id}`)
}

export function createTenant(data: Partial<Tenant>) {
  return request.post('/admin/tenants', data)
}

export function updateTenant(id: number | string, data: Partial<Tenant>) {
  return request.put(`/admin/tenants/${id}`, data)
}

export function auditTenant(id: number | string, status: 'APPROVED' | 'REJECTED') {
  return request.put(`/admin/tenants/${id}/audit`, { status })
}

export function deleteTenant(id: number | string) {
  return request.delete(`/admin/tenants/${id}`)
}

export function getUsers(params?: { userType?: string; tenantId?: number; page?: number; size?: number }) {
  return request.get<unknown, PageResult<AdminUser>>('/admin/users', { params })
}

export function getUser(id: number | string) {
  return request.get<unknown, AdminUser>(`/admin/users/${id}`)
}

export function createUser(data: AdminUserPayload) {
  return request.post('/admin/users', data)
}

export function updateUser(id: number | string, data: Partial<AdminUserPayload>) {
  return request.put<unknown, AdminUser>(`/admin/users/${id}`, data)
}

export function deleteUser(id: number | string) {
  return request.delete(`/admin/users/${id}`)
}

export function getGlobalConfig() {
  return request.get('/admin/config')
}

export function updateGlobalConfig(data: Record<string, string>) {
  return request.put('/admin/config', data)
}

export function testSmtpEmail(to: string) {
  return request.post('/admin/config/test-email', { to })
}

export function getAdminEmailLogs(params?: { page?: number; size?: number }) {
  return request.get<unknown, { records: { id: number; code: string; recipient: string; subject: string; status: string; errorMsg?: string; retryCount: number; createdAt: string }[]; total: number }>('/admin/config/email-logs', { params })
}
