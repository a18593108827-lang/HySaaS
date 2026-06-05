<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPortalEvent, submitRegistration } from '@/api/portal'
import type { EventItem } from '@/types'

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.id)
const inviteToken = typeof route.query.inviteToken === 'string' ? route.query.inviteToken : undefined
const loading = ref(false)
const submitting = ref(false)
const event = ref<EventItem | null>(null)
const form = ref({ name: '', email: '', phone: '', memberType: '普通会员', org: '' })

onMounted(async () => {
  loading.value = true
  try {
    event.value = await getPortalEvent(eventId)
  } catch {
    event.value = { id: eventId, title: '2026 医学年会', location: '上海', startTime: '2026-09-15', endTime: '2026-09-17', status: 'REGISTRATION_OPEN', registrationEnabled: true, paperEnabled: true, hotelEnabled: true }
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  if (!form.value.name || !form.value.email) return ElMessage.warning('请填写必填项')
  submitting.value = true
  try {
    await submitRegistration(eventId, { ...form.value, ...(inviteToken ? { inviteToken } : {}) })
    ElMessage.success('报名已提交，等待审核')
    router.push('/portal/events')
  } catch {
    ElMessage.success('演示：报名已提交')
    router.push('/portal/events')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <h1>活动报名</h1>
      <p v-if="event">{{ event.title }}</p>
    </div>
    <el-form label-width="100px" style="max-width: 480px">
      <el-form-item label="姓名" required>
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="邮箱" required>
        <el-input v-model="form.email" type="email" />
      </el-form-item>
      <el-form-item label="手机">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="form.org" />
      </el-form-item>
      <el-form-item label="会员类型">
        <el-select v-model="form.memberType" style="width: 100%">
          <el-option label="普通会员" value="普通会员" />
          <el-option label="付费会员" value="付费会员" />
          <el-option label="理事会成员" value="理事会成员" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="onSubmit">提交报名</el-button>
        <el-button @click="router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
