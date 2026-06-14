import QRCode from 'qrcode'

export interface InviteLinkData {
  eventId: number | string
  eventTitle?: string
  token: string
  inviteUrl: string
  qrcodeDataUrl: string
}

const tokenKey = (eventId: number | string) => `invite-token-${eventId}`

export function getStoredInviteToken(eventId: number | string) {
  return localStorage.getItem(tokenKey(eventId))
}

export function storeInviteToken(eventId: number | string, token: string) {
  localStorage.setItem(tokenKey(eventId), token)
}

export function createInviteToken(eventId: number | string) {
  const existing = getStoredInviteToken(eventId)
  if (existing) return existing
  const token = `inv-${eventId}-${Date.now().toString(36)}`
  storeInviteToken(eventId, token)
  return token
}

export function buildInviteUrl(eventId: number | string, token: string) {
  const url = new URL(`/event/${eventId}/register`, window.location.origin)
  url.searchParams.set('inviteToken', token)
  return url.href
}

export async function buildInviteLinkData(eventId: number | string, eventTitle?: string, token?: string) {
  const t = token || createInviteToken(eventId)
  const inviteUrl = buildInviteUrl(eventId, t)
  const qrcodeDataUrl = await QRCode.toDataURL(inviteUrl, {
    width: 240,
    margin: 2,
    color: { dark: '#000b47', light: '#ffffff' },
  })
  return { eventId, eventTitle, token: t, inviteUrl, qrcodeDataUrl }
}

export async function copyText(text: string) {
  await navigator.clipboard.writeText(text)
}
