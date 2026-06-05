import request from './request'
import type { TenantApplyPayload } from '@/types'

export function applyTenant(data: TenantApplyPayload) {
  return request.post('/public/tenant/apply', data)
}
