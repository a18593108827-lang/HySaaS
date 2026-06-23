<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  codeUrl: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const qrSrc = computed(() =>
  props.codeUrl
    ? `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(props.codeUrl)}`
    : '',
)
</script>

<template>
  <el-dialog v-model="visible" title="微信扫码支付" width="320px" align-center>
    <div class="qr-wrap">
      <img v-if="qrSrc" :src="qrSrc" alt="微信支付二维码" width="220" height="220" />
      <p>请使用微信扫一扫完成支付</p>
    </div>
  </el-dialog>
</template>

<style scoped>
.qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
  color: var(--muted);
  font-size: 0.875rem;
}
</style>
