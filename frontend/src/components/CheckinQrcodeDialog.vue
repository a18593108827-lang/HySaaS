<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { generateQrcode } from '@/api/enterprise'
import {
  buildCheckinQrcode,
  copyText,
  createCheckinToken,
  downloadDataUrl,
  storeCheckinToken,
  type CheckinQrcodeData,
} from '@/utils/checkin'

const props = defineProps<{
  modelValue: boolean
  eventId: number | string
  eventTitle?: string
  token?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const loading = ref(false)
const data = ref<CheckinQrcodeData | null>(null)

async function load() {
  loading.value = true
  data.value = null
  try {
    let token = createCheckinToken(props.eventId)
    try {
      const res = await generateQrcode(props.eventId)
      if (res.token) {
        token = res.token
        storeCheckinToken(props.eventId, res.token)
      }
    } catch {
      // 接口失败时使用本地 token 生成二维码
    }
    data.value = await buildCheckinQrcode(props.eventId, props.eventTitle, token)
  } catch {
    ElMessage.error('二维码生成失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) load()
  },
  { immediate: true },
)

async function onCopy() {
  if (!data.value) return
  await copyText(data.value.checkinUrl)
  ElMessage.success('链接已复制')
}

function onDownload() {
  if (!data.value) return
  downloadDataUrl(data.value.qrcodeDataUrl, `checkin-${props.eventId}.png`)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="会场签到二维码"
    width="420px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="qrcode-panel">
      <template v-if="data">
        <p class="hint">参会人扫码后打开网页，登录即自动签到</p>
        <img :src="data.qrcodeDataUrl" alt="签到二维码" class="qrcode-img" />
        <p v-if="eventTitle" class="event-title">{{ eventTitle }}</p>
        <el-input :model-value="data.checkinUrl" readonly>
          <template #append>
            <el-button @click="onCopy">复制</el-button>
          </template>
        </el-input>
      </template>
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" :disabled="!data" @click="onDownload">下载二维码</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.qrcode-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  min-height: 200px;
}

.hint {
  margin: 0;
  font-size: 0.875rem;
  color: var(--muted);
  text-align: center;
}

.qrcode-img {
  width: 280px;
  height: 280px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.event-title {
  margin: 0;
  font-weight: 600;
}

.qrcode-panel :deep(.el-input) {
  width: 100%;
}
</style>
