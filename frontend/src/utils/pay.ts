export interface WechatJsapiPay {
  appId: string
  timeStamp: string
  nonceStr: string
  packageVal: string
  signType: string
  paySign: string
}

export interface PayCreateResult {
  payMode?: 'MOCK' | 'ALIPAY' | 'WECHAT'
  payUrl?: string
  payForm?: string
  codeUrl?: string
  wechatJsapi?: WechatJsapiPay
}

export interface PayMethods {
  mock: boolean
  alipay: boolean
  wechat: boolean
}

export type PayProvider = 'alipay' | 'wechat'

declare global {
  interface Window {
    WeixinJSBridge?: {
      invoke: (
        api: string,
        params: Record<string, string>,
        cb: (res: { err_msg?: string }) => void,
      ) => void
    }
  }
}

export function isMobilePayChannel() {
  return /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent)
}

export function isWeChatBrowser() {
  return /MicroMessenger/i.test(navigator.userAgent)
}

export function resolveAlipayChannel() {
  return isMobilePayChannel() ? 'wap' : 'page'
}

export function resolveWechatChannel(openid?: string) {
  if (isWeChatBrowser() && openid) return 'jsapi'
  if (isMobilePayChannel()) return 'h5'
  return 'native'
}

export function buildPayPayload(provider: PayProvider, openid?: string) {
  if (provider === 'wechat') {
    const channel = resolveWechatChannel(openid)
    return { provider, channel, ...(channel === 'jsapi' && openid ? { openid } : {}) }
  }
  return { provider, channel: resolveAlipayChannel() }
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

export function invokeWechatJsapi(params: WechatJsapiPay): Promise<void> {
  return new Promise((resolve, reject) => {
    const onBridgeReady = () => {
      window.WeixinJSBridge!.invoke(
        'getBrandWCPayRequest',
        {
          appId: params.appId,
          timeStamp: params.timeStamp,
          nonceStr: params.nonceStr,
          package: params.packageVal,
          signType: params.signType,
          paySign: params.paySign,
        },
        (res) => {
          if (res.err_msg === 'get_brand_wcpay_request:ok') resolve()
          else reject(new Error(res.err_msg || '支付取消'))
        },
      )
    }
    if (typeof window.WeixinJSBridge === 'undefined') {
      document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false)
    } else {
      onBridgeReady()
    }
  })
}

export async function launchPay(
  create: (payload: ReturnType<typeof buildPayPayload>) => Promise<PayCreateResult>,
  options: {
    provider: PayProvider
    mockPay?: () => Promise<unknown>
    openid?: string
    onNativeQr?: (codeUrl: string) => void
  },
): Promise<'alipay' | 'wechat' | 'mock' | void> {
  const payload = buildPayPayload(options.provider, options.openid)
  const res = await create(payload)
  if (res.payForm) {
    submitAlipayForm(res.payForm)
    return 'alipay'
  }
  if (res.codeUrl) {
    options.onNativeQr?.(res.codeUrl)
    return 'wechat'
  }
  if (res.payUrl && res.payMode === 'WECHAT') {
    window.location.href = res.payUrl
    return 'wechat'
  }
  if (res.wechatJsapi) {
    await invokeWechatJsapi(res.wechatJsapi)
    return 'wechat'
  }
  if (res.payMode === 'MOCK' && options.mockPay) {
    await options.mockPay()
    return 'mock'
  }
}

export function resolvePayProvider(methods: PayMethods): PayProvider | 'mock' {
  if (methods.mock) return 'mock'
  if (methods.alipay && !methods.wechat) return 'alipay'
  if (methods.wechat && !methods.alipay) return 'wechat'
  return 'alipay'
}

export function needPayProviderChoice(methods: PayMethods) {
  return !methods.mock && methods.alipay && methods.wechat
}
