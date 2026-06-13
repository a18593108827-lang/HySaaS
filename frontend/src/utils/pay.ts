export interface PayCreateResult {
  payMode?: 'MOCK' | 'ALIPAY'
  payUrl?: string
  payForm?: string
}

export function isMobilePayChannel() {
  return /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent)
}

export function submitAlipayForm(html: string) {
  const wrap = document.createElement('div')
  wrap.style.display = 'none'
  wrap.innerHTML = html
  document.body.appendChild(wrap)
  const form = wrap.querySelector('form')
  if (!form) {
    document.body.removeChild(wrap)
    throw new Error('支付表单无效')
  }
  form.submit()
}

export async function launchPay(
  create: () => Promise<PayCreateResult>,
  mockPay?: () => Promise<unknown>,
): Promise<'alipay' | 'mock' | void> {
  const res = await create()
  if (res.payForm) {
    submitAlipayForm(res.payForm)
    return 'alipay'
  }
  if (res.payMode === 'MOCK' && mockPay) {
    await mockPay()
    return 'mock'
  }
}
