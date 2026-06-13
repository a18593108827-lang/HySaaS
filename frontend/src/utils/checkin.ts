import QRCode from 'qrcode'

export interface CheckinQrcodeData {
  eventId: number | string
  eventTitle?: string
  token: string
  checkinUrl: string
  qrcodeDataUrl: string
}

const tokenKey = (eventId: number | string) => `checkin-token-${eventId}`

export function getStoredCheckinToken(eventId: number | string) {
  return localStorage.getItem(tokenKey(eventId))
}

export function storeCheckinToken(eventId: number | string, token: string) {
  localStorage.setItem(tokenKey(eventId), token)
}

export function createCheckinToken(eventId: number | string) {
  const existing = getStoredCheckinToken(eventId)
  if (existing) return existing
  const token = `${eventId}-${Date.now().toString(36)}`
  storeCheckinToken(eventId, token)
  return token
}

export function buildCheckinUrl(eventId: number | string, token: string) {
  const url = new URL(`/checkin/${eventId}`, window.location.origin)
  url.searchParams.set('token', token)
  return url.href
}

export async function buildCheckinQrcode(eventId: number | string, eventTitle?: string, token?: string) {
  const t = token || createCheckinToken(eventId)
  const checkinUrl = buildCheckinUrl(eventId, t)
  const qrcodeDataUrl = await QRCode.toDataURL(checkinUrl, {
    width: 280,
    margin: 2,
    color: { dark: '#000b47', light: '#ffffff' },
  })
  return { eventId, eventTitle, token: t, checkinUrl, qrcodeDataUrl }
}

export async function copyText(text: string) {
  await navigator.clipboard.writeText(text)
}

export function downloadDataUrl(dataUrl: string, filename: string) {
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = filename
  a.click()
}
