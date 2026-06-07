export type UserType = 'PLATFORM' | 'ENTERPRISE' | 'ATTENDEE'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  userType: UserType
  tenantId?: number
  roles: string[]
  eventPermissions: string[]
}

export interface LoginResult {
  token: string
  userType: UserType
}

export interface AdminUser {
  id: number
  username: string
  nickname: string
  userType: UserType
  tenantId?: number
  tenantName?: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}

export interface AdminUserPayload {
  username: string
  nickname: string
  userType: UserType
  tenantId?: number
  password?: string
  status?: 'ENABLED' | 'DISABLED'
}

export interface EnterpriseMember {
  id: number
  username: string
  nickname: string
  roles: string[]
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}

export interface EnterpriseMemberPayload {
  username: string
  nickname: string
  roles: string[]
  password?: string
  status?: 'ENABLED' | 'DISABLED'
}

export interface EnterpriseAttendee {
  id: number
  username: string
  nickname: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}

export interface EnterpriseAttendeePayload {
  username: string
  nickname: string
  password?: string
  status?: 'ENABLED' | 'DISABLED'
}

export interface Tenant {
  id: number
  name: string
  contactName: string
  contactPhone: string
  contactEmail?: string
  address?: string
  remark?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
  updatedAt?: string
}

export interface TenantApplyPayload {
  name: string
  contactName: string
  contactPhone: string
  contactEmail: string
  address?: string
  remark?: string
}

export interface CheckinQrcodeResult {
  qrcodeUrl?: string
  checkinUrl?: string
  token?: string
}

export interface EventItem {
  id: number
  title: string
  location: string
  startTime: string
  endTime: string
  status: 'DRAFT' | 'PUBLISHED' | 'REGISTRATION_OPEN' | 'REGISTRATION_CLOSED'
  registrationEnabled: boolean
  paperEnabled: boolean
  hotelEnabled: boolean
  inviteUrl?: string
  qrcodeUrl?: string
  checkinToken?: string
}

export interface Registration {
  id: number
  eventId: number
  userId?: number
  name: string
  email: string
  phone: string
  memberType: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  source?: 'SELF' | 'INVITE' | 'INVITE_LINK'
  createdAt: string
}

export interface EventInvitePayload {
  userIds: number[]
  autoApprove?: boolean
}

export interface EventInviteResult {
  invited: number
  skipped?: number
}

export interface EventInviteLinkResult {
  token?: string
  inviteUrl?: string
}

export interface PaperSubmission {
  id: number
  title: string
  author: string
  status: string
  version: number
  submittedAt: string
}

export interface HotelInfo {
  id: number
  name: string
  address: string
  contactPhone: string
}

export interface HotelInfoPayload {
  name: string
  address: string
  contactPhone: string
}

export interface HotelRoomType {
  id: number
  hotelId: number
  name: string
  price: number
  quota: number
  used?: number
}

export interface HotelRoomTypePayload {
  name: string
  price: number
  quota: number
}

export interface HotelBooking {
  id: number
  bookingNo: string
  eventId: number
  eventTitle: string
  guestName: string
  hotelName: string
  roomTypeName: string
  nights: number
  amount: number
  status: 'PENDING_PAY' | 'LOCKED' | 'CHECKED_IN' | 'CANCELLED'
  createdAt: string
  checkedInAt?: string
}

export interface PayOrder {
  id: number
  orderNo: string
  bizType: 'REGISTRATION' | 'HOTEL' | string
  amount: number
  status: 'PENDING' | 'PAID' | 'CLOSED' | 'CANCELLED' | string
  invoiceStatus?: 'NONE' | 'APPLYING' | 'ISSUED'
  createdAt: string
}

export interface InvoiceApplyPayload {
  orderId: number
  title: string
  taxNo: string
  email: string
}

export interface PageResult<T> {
  records: T[]
  total: number
}

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}
