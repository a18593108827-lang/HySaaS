<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerAttendee } from '@/api/public'
import { getPortalEvent } from '@/api/portal'
import { useAuthStore } from '@/stores/auth'
import { validateEmail, validatePhone } from '@/utils/account'
import type { EventItem } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const eventId = computed(() => String(route.query.eventId || ''))
const loading = ref(false)
const submitting = ref(false)
const event = ref<EventItem | null>(null)
const form = ref({ nickname: '', email: '', phone: '', password: '', confirm: '' })
const emailError = ref('')
const phoneError = ref('')

onMounted(async () => {
  if (!eventId.value) {
    ElMessage.error('缺少活动信息')
    router.replace('/login')
    return
  }
  loading.value = true
  try {
    event.value = await getPortalEvent(eventId.value)
  } catch {
    ElMessage.error('活动不存在或未开放')
    router.replace('/login')
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (!form.value.nickname.trim()) return ElMessage.warning('请填写姓名')
  if (emailError.value || phoneError.value) return
  if (form.value.password.length < 6) return ElMessage.warning('密码至少 6 位')
  if (form.value.password !== form.value.confirm) return ElMessage.warning('两次密码不一致')
  submitting.value = true
  try {
    const res = await registerAttendee({
      eventId: eventId.value,
      nickname: form.value.nickname.trim(),
      email: form.value.email.trim(),
      phone: form.value.phone.trim(),
      password: form.value.password,
    })
    auth.setToken(res.token)
    await auth.fetchUser()
    ElMessage.success('注册成功')
    router.replace(`/portal/events/${eventId.value}/register`)
  } catch {
    return
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="shell">
    <aside class="brand">
      <div class="logo">
        <span class="logo-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 8.5v7M8 6v12M12 9v6M16 7v10M20 8.5v7" stroke="white" stroke-width="2" stroke-linecap="round" />
          </svg>
        </span>
        HySaaS
      </div>
      <div class="brand-main">
        <h1>参会注册</h1>
        <p v-if="event">注册后可报名「{{ event.title }}」</p>
      </div>
    </aside>

    <main class="main">
      <div v-loading="loading" class="card">
        <header class="card-header">
          <h2>创建参会账号</h2>
          <p>邮箱与手机将用于登录</p>
        </header>
        <el-form label-width="80px" @submit.prevent="onSubmit">
          <el-form-item label="姓名" required>
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="邮箱" required :error="emailError">
            <el-input v-model="form.email" type="email" />
          </el-form-item>
          <el-form-item label="手机" required :error="phoneError">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" required>
            <el-input v-model="form.confirm" type="password" show-password />
          </el-form-item>
          <el-button type="primary" native-type="submit" class="submit-btn" :loading="submitting">注册并继续</el-button>
        </el-form>
        <p class="footer-note">已有账号？<router-link class="link" :to="{ path: '/login', query: { redirect: `/portal/events/${eventId}/register` } }">直接登录</router-link></p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.shell { display: flex; min-height: 100dvh; }
.brand {
  flex: 0 0 42%; max-width: 520px;
  background: linear-gradient(160deg, var(--navy) 0%, var(--navy-mid) 55%, #122d8a 100%);
  color: var(--panel-ink); padding: clamp(2rem, 5vw, 3.5rem);
  display: flex; flex-direction: column; justify-content: space-between;
}
.logo { display: flex; align-items: center; gap: 0.75rem; font-weight: 700; font-size: 1.125rem; }
.logo-mark { width: 36px; height: 36px; border-radius: 10px; background: var(--primary); display: grid; place-items: center; }
.logo-mark svg { width: 20px; height: 20px; }
.brand-main h1 { font-size: 2rem; margin: 0 0 1rem; }
.brand-main p { color: var(--panel-muted); margin: 0; }
.main { flex: 1; display: flex; align-items: center; justify-content: center; padding: 2rem; background: var(--surface); }
.card { width: 100%; max-width: 440px; padding: 2rem; background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius); }
.card-header { margin-bottom: 1.5rem; }
.card-header h2 { margin: 0 0 0.35rem; font-size: 1.5rem; }
.card-header p { margin: 0; color: var(--muted); font-size: 0.9375rem; }
.submit-btn { width: 100%; margin-top: 0.5rem; }
.footer-note { margin-top: 1.5rem; text-align: center; font-size: 0.875rem; color: var(--muted); }
.link { color: var(--primary); text-decoration: none; }
@media (max-width: 860px) { .shell { flex-direction: column; } .brand { flex: none; max-width: none; } }
</style>
