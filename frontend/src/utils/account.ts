const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phoneRe = /^1\d{10}$/

export function validEmail(v: string) {
  return emailRe.test(v.trim())
}

export function validPhone(v: string) {
  return phoneRe.test(v.trim())
}

export function validAccount(v: string) {
  return validEmail(v) || validPhone(v)
}

export function validateEmail(v: string) {
  if (!v.trim()) return '请输入邮箱'
  if (!validEmail(v)) return '请输入有效的邮箱'
  return ''
}

export function validatePhone(v: string) {
  if (!v.trim()) return '请输入手机号'
  if (!validPhone(v)) return '请输入有效的手机号'
  return ''
}

export function validateAccount(v: string) {
  if (!v.trim()) return '请输入邮箱或手机号'
  if (!validAccount(v)) return '请输入有效的邮箱或手机号'
  return ''
}

export function validatePassword(v: string, required = true) {
  if (!v) return required ? '请输入密码' : ''
  if (v.length < 6) return '密码至少 6 位'
  return ''
}
