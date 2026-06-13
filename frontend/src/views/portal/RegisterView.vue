<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getPortalEvent, submitRegistration } from '@/api/portal'
import { validateEmail, validatePhone } from '@/utils/account'
import PortalBackBar from '@/components/PortalBackBar.vue'
import type { EventItem } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const eventId = String(route.params.id)
const inviteToken = typeof route.query.inviteToken === 'string' ? route.query.inviteToken : undefined
const loading = ref(false)
const submitting = ref(false)
const event = ref<EventItem | null>(null)
const form = ref({ name: '', email: '', phone: '', memberType: '普通会员', org: '' })
const emailError = ref('')
const phoneError = ref('')

onMounted(async () => {
  loading.value = true
  try {
    event.value = await getPortalEvent(eventId)
  } catch {
    ElMessage.error('加载活动信息失败')
    router.replace('/portal/events')
  } finally {
    loading.value = false
  }
  if (auth.user) {
    form.value.name = auth.user.nickname || ''
    form.value.email = auth.user.email || auth.user.username || ''
    form.value.phone = auth.user.phone || ''
  }
})

async function onSubmit() {
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (!form.value.name) return ElMessage.warning('请填写姓名')
  if (emailError.value || phoneError.value) return
  submitting.value = true
  try {
    await submitRegistration(eventId, { ...form.value, ...(inviteToken ? { inviteToken } : {}) })
    ElMessage.success('报名已提交，等待审核')
    router.push('/portal/events')
  } catch {
    return
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <PortalBackBar />
    <div class="page-header">
      <h1>活动报名</h1>
      <p v-if="event">{{ event.title }}</p>
    </div>
    <el-form label-width="100px" style="max-width: 480px">
      <el-form-item label="姓名" required>
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="邮箱" required :error="emailError">
        <el-input v-model="form.email" type="email" />
      </el-form-item>
      <el-form-item label="手机" required :error="phoneError">
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
      </el-form-item>
    </el-form>
  </div>
</template>
