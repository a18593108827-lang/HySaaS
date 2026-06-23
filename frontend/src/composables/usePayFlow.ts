import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayOrder, getPayMethods, mockPay } from '@/api/portal'
import {
  launchPay,
  needPayProviderChoice,
  resolvePayProvider,
  type PayMethods,
  type PayProvider,
} from '@/utils/pay'

interface PayAction {
  bizType: string
  bizId: number | string
  onMockSuccess?: () => void | Promise<unknown>
  onPaid?: () => void | Promise<unknown>
}

export function usePayFlow() {
  const providerDialogVisible = ref(false)
  const qrDialogVisible = ref(false)
  const qrCodeUrl = ref('')
  const payMethods = ref<PayMethods>({ mock: false, alipay: false, wechat: false })
  let pendingAction: PayAction | null = null

  async function runPay(provider: PayProvider | 'mock', action: PayAction) {
    const mode = await launchPay(
      (payload) =>
        createPayOrder({
          bizType: action.bizType,
          bizId: action.bizId,
          ...payload,
        }),
      {
        provider: provider === 'mock' ? 'alipay' : provider,
        mockPay: () => mockPay(action.bizId),
        onNativeQr: (url) => {
          qrCodeUrl.value = url
          qrDialogVisible.value = true
        },
      },
    )
    if (mode === 'mock') {
      await action.onMockSuccess?.()
      return mode
    }
    if (mode === 'alipay') {
      ElMessage.success('正在跳转支付宝…')
      return mode
    }
    if (mode === 'wechat') {
      if (qrDialogVisible.value) {
        ElMessage.success('请微信扫码完成支付')
      } else {
        await action.onPaid?.()
        ElMessage.success('支付成功')
      }
      return mode
    }
  }

  async function startPay(action: PayAction) {
    payMethods.value = await getPayMethods()
    if (needPayProviderChoice(payMethods.value)) {
      pendingAction = action
      providerDialogVisible.value = true
      return undefined
    }
    return runPay(resolvePayProvider(payMethods.value), action)
  }

  async function onProviderSelect(provider: PayProvider) {
    if (!pendingAction) return undefined
    const action = pendingAction
    pendingAction = null
    return runPay(provider, action)
  }

  return {
    providerDialogVisible,
    qrDialogVisible,
    qrCodeUrl,
    payMethods,
    startPay,
    onProviderSelect,
  }
}
