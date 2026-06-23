<script setup lang="ts">
import type { PayMethods, PayProvider } from '@/utils/pay'

defineProps<{
  modelValue: boolean
  methods: PayMethods
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [provider: PayProvider]
}>()

function pick(provider: PayProvider) {
  emit('select', provider)
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="选择支付方式" width="360px" align-center @update:model-value="$emit('update:modelValue', $event)">
    <div class="pay-options">
      <el-button v-if="methods.alipay" type="primary" plain @click="pick('alipay')">支付宝</el-button>
      <el-button v-if="methods.wechat" type="success" plain @click="pick('wechat')">微信支付</el-button>
    </div>
  </el-dialog>
</template>

<style scoped>
.pay-options {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 8px 0 16px;
}
</style>
