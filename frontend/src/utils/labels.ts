export const payBizTypeMap: Record<string, string> = {
  REGISTRATION: '报名费',
  HOTEL: '酒店预订',
}

export const payStatusMap: Record<string, { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
  PENDING: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' },
  CANCELLED: { label: '已取消', type: 'info' },
}

export const invoiceStatusMap: Record<string, string> = {
  NONE: '未申请',
  APPLYING: '开票中',
  ISSUED: '已开票',
  PENDING: '开票中',
  SUCCESS: '已开票',
  FAILED: '开票失败',
}
