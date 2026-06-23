<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Location, Money, Document, House, Ticket } from '@element-plus/icons-vue'
import { registerAttendee } from '@/api/public'
import { getPortalEvent, submitRegistration } from '@/api/portal'
import { useAuthStore } from '@/stores/auth'
import PayProviderDialog from '@/components/PayProviderDialog.vue'
import WechatQrDialog from '@/components/WechatQrDialog.vue'
import { usePayFlow } from '@/composables/usePayFlow'
import { validateEmail, validatePhone } from '@/utils/account'
import type { EventItem } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const {
  providerDialogVisible,
  qrDialogVisible,
  qrCodeUrl,
  payMethods,
  startPay,
  onProviderSelect,
} = usePayFlow()

const eventId = computed(() => String(route.params.id))
const inviteToken = computed(() => (typeof route.query.inviteToken === 'string' ? route.query.inviteToken : undefined))
const loading = ref(false)
const submitting = ref(false)
const event = ref<EventItem | null>(null)
const form = ref({ name: '', email: '', phone: '', memberType: '普通会员', org: '', password: '', confirm: '' })
const emailError = ref('')
const phoneError = ref('')

const isAttendee = computed(() => auth.user?.userType === 'ATTENDEE')
const needAccount = computed(() => !auth.token || !isAttendee.value)

const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'info' }> = {
  PUBLISHED: { label: '已发布', type: 'success' },
  REGISTRATION_OPEN: { label: '报名中', type: 'success' },
  REGISTRATION_CLOSED: { label: '报名截止', type: 'warning' },
}

const regStatusMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
}

const durationDays = computed(() => {
  if (!event.value?.startTime || !event.value?.endTime) return null
  const s = new Date(event.value.startTime)
  const e = new Date(event.value.endTime)
  const days = Math.round((e.getTime() - s.getTime()) / 86400000) + 1
  return days > 0 ? days : 1
})

onMounted(async () => {
  if (auth.token && !auth.user) {
    try { await auth.fetchUser() } catch { auth.clearAuth() }
  }
  loading.value = true
  try {
    event.value = await getPortalEvent(eventId.value)
    if (!event.value.registrationEnabled) {
      ElMessage.warning('该活动未开放报名')
    }
  } catch {
    ElMessage.error('活动不存在或未开放')
  } finally {
    loading.value = false
  }
  if (isAttendee.value && auth.user) {
    form.value.name = auth.user.nickname || ''
    form.value.email = auth.user.email || auth.user.username || ''
    form.value.phone = auth.user.phone || ''
  }
})

async function ensureAttendee() {
  if (isAttendee.value) return
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (!form.value.name.trim()) throw new Error('请填写姓名')
  if (emailError.value || phoneError.value) throw new Error('invalid')
  if (form.value.password.length < 6) throw new Error('密码至少 6 位')
  if (form.value.password !== form.value.confirm) throw new Error('两次密码不一致')
  const res = await registerAttendee({
    eventId: eventId.value,
    nickname: form.value.name.trim(),
    email: form.value.email.trim(),
    phone: form.value.phone.trim(),
    password: form.value.password,
  })
  auth.setToken(res.token)
  await auth.fetchUser()
}

async function onSubmit() {
  emailError.value = validateEmail(form.value.email)
  phoneError.value = validatePhone(form.value.phone)
  if (!form.value.name.trim()) return ElMessage.warning('请填写姓名')
  if (emailError.value || phoneError.value) return
  if (auth.token && auth.user && !isAttendee.value) {
    return ElMessage.warning('请使用参会人账号，或退出后重新注册')
  }
  submitting.value = true
  try {
    if (needAccount.value) await ensureAttendee()
    const result = await submitRegistration(eventId.value, {
      name: form.value.name,
      email: form.value.email,
      phone: form.value.phone,
      memberType: form.value.memberType,
      ...(inviteToken.value ? { inviteToken: inviteToken.value } : {}),
    })
    if (result.payOrder) {
      const mode = await startPay({
        bizType: 'REGISTRATION',
        bizId: result.payOrder.id,
        onMockSuccess: () => router.push('/portal/events'),
      })
      if (mode === 'mock') {
        ElMessage.success('报名已提交并完成支付，等待审核')
        router.push('/portal/events')
      }
    } else {
      ElMessage.success('报名已提交，等待审核')
      router.push('/portal/events')
    }
  } catch (e: unknown) {
    const msg = (e as { message?: string })?.message
    if (msg && msg !== 'invalid') ElMessage.warning(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <header class="topbar">
      <div class="topbar-inner">
        <span class="logo-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none"><path d="M4 8.5v7M8 6v12M12 9v6M16 7v10M20 8.5v7" stroke="currentColor" stroke-width="2" stroke-linecap="round" /></svg>
        </span>
        <span class="logo-text">HySaaS</span>
      </div>
    </header>

    <div v-loading="loading" class="content">
      <section v-if="event" class="hero">
        <div class="hero-inner">
          <div class="hero-head">
            <el-tag v-if="statusMap[event.status]" :type="statusMap[event.status].type" effect="dark" round>
              {{ statusMap[event.status].label }}
            </el-tag>
            <el-tag v-if="event.myRegistrationStatus" type="info" round>
              我的报名：{{ regStatusMap[event.myRegistrationStatus] || event.myRegistrationStatus }}
            </el-tag>
          </div>
          <h1>{{ event.title }}</h1>
          <div class="info-grid">
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <div>
                <span class="info-label">开始日期</span>
                <span class="info-value">{{ event.startTime || '待定' }}</span>
              </div>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <div>
                <span class="info-label">结束日期</span>
                <span class="info-value">{{ event.endTime || '待定' }}</span>
              </div>
            </div>
            <div v-if="durationDays" class="info-item">
              <el-icon><Ticket /></el-icon>
              <div>
                <span class="info-label">会期</span>
                <span class="info-value">{{ durationDays }} 天</span>
              </div>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <div>
                <span class="info-label">活动地点</span>
                <span class="info-value">{{ event.location || '待定' }}</span>
              </div>
            </div>
            <div class="info-item">
              <el-icon><Money /></el-icon>
              <div>
                <span class="info-label">报名费用</span>
                <span class="info-value">{{ event.registrationFee ? `¥${event.registrationFee}` : '免费' }}</span>
              </div>
            </div>
          </div>
          <div class="feature-row">
            <el-tag v-if="event.registrationEnabled" type="primary" effect="plain">开放报名</el-tag>
            <el-tag v-if="event.paperEnabled" effect="plain"><el-icon><Document /></el-icon> 论文投稿</el-tag>
            <el-tag v-if="event.hotelEnabled" effect="plain"><el-icon><House /></el-icon> 酒店预订</el-tag>
          </div>
        </div>
      </section>

      <div class="body">
        <aside v-if="event" class="summary">
          <h3>活动概要</h3>
          <dl>
            <dt>活动名称</dt>
            <dd>{{ event.title }}</dd>
            <dt>举办时间</dt>
            <dd>{{ event.startTime }} 至 {{ event.endTime }}</dd>
            <dt>举办地点</dt>
            <dd>{{ event.location || '待定' }}</dd>
            <dt>报名费用</dt>
            <dd>{{ event.registrationFee ? `¥${event.registrationFee}` : '免费参加' }}</dd>
            <dt>开放服务</dt>
            <dd class="service-tags">
              <span v-if="event.registrationEnabled">在线报名</span>
              <span v-if="event.paperEnabled">论文投稿</span>
              <span v-if="event.hotelEnabled">协议酒店</span>
              <span v-if="!event.registrationEnabled && !event.paperEnabled && !event.hotelEnabled">—</span>
            </dd>
          </dl>
        </aside>

        <section class="form-panel">
          <div class="form-card">
            <header class="form-header">
              <h2>填写报名信息</h2>
              <p v-if="needAccount">提交后将自动创建参会账号</p>
              <p v-else>{{ auth.user?.nickname }}，请确认信息后提交</p>
            </header>

            <el-alert v-if="auth.user && !isAttendee" type="warning" show-icon :closable="false" class="alert">
              当前为企业/平台账号，请<router-link :to="{ path: '/login', query: { redirect: route.fullPath } }">切换参会账号登录</router-link>
            </el-alert>

            <el-alert v-if="event?.myRegistrationStatus === 'APPROVED'" type="success" show-icon :closable="false" class="alert">
              您已通过审核，无需重复报名
            </el-alert>

            <el-form v-else label-width="88px" @submit.prevent="onSubmit">
              <el-form-item label="姓名" required>
                <el-input v-model="form.name" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="邮箱" required :error="emailError">
                <el-input v-model="form.email" type="email" placeholder="用于接收通知" :disabled="isAttendee" />
              </el-form-item>
              <el-form-item label="手机" required :error="phoneError">
                <el-input v-model="form.phone" placeholder="11 位手机号" :disabled="isAttendee" />
              </el-form-item>
              <el-form-item label="单位">
                <el-input v-model="form.org" placeholder="工作单位（选填）" />
              </el-form-item>
              <el-form-item label="会员类型">
                <el-select v-model="form.memberType" style="width: 100%">
                  <el-option label="普通会员" value="普通会员" />
                  <el-option label="付费会员" value="付费会员" />
                  <el-option label="理事会成员" value="理事会成员" />
                </el-select>
              </el-form-item>
              <template v-if="needAccount">
                <el-form-item label="密码" required>
                  <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
                </el-form-item>
                <el-form-item label="确认密码" required>
                  <el-input v-model="form.confirm" type="password" show-password />
                </el-form-item>
              </template>
              <el-button type="primary" native-type="submit" class="submit-btn" :loading="submitting" :disabled="!event?.registrationEnabled">
                {{ event?.registrationFee ? '提交并支付' : '提交报名' }}
              </el-button>
            </el-form>

            <p v-if="needAccount && !event?.myRegistrationStatus" class="footer-note">
              已有账号？<router-link class="link" :to="{ path: '/login', query: { redirect: route.fullPath } }">直接登录</router-link>
            </p>
            <p v-else-if="isAttendee" class="footer-note">
              <router-link class="link" to="/portal/events">进入参会中心</router-link>
            </p>
          </div>
        </section>
      </div>
    </div>
    <PayProviderDialog
      v-model="providerDialogVisible"
      :methods="payMethods"
      @select="onProviderSelect"
    />
    <WechatQrDialog v-model="qrDialogVisible" :code-url="qrCodeUrl" />
  </div>
</template>

<style scoped>
.page { min-height: 100dvh; background: var(--surface); }
.topbar { background: var(--bg); border-bottom: 1px solid var(--border); }
.topbar-inner { max-width: 1080px; margin: 0 auto; padding: 0.875rem 1.5rem; display: flex; align-items: center; gap: 0.625rem; }
.logo-mark { width: 28px; height: 28px; border-radius: 8px; background: var(--primary); color: #fff; display: grid; place-items: center; }
.logo-mark svg { width: 16px; height: 16px; }
.logo-text { font-weight: 700; font-size: 1rem; color: var(--navy); }
.content { max-width: 1080px; margin: 0 auto; padding: 0 1.5rem 3rem; }
.hero {
  margin: 1.5rem 0;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--navy) 0%, var(--navy-mid) 60%, #1a3fb8 100%);
  color: #fff;
  overflow: hidden;
  position: relative;
}
.hero::after {
  content: '';
  position: absolute;
  inset: -30% -10% auto auto;
  width: 50%;
  height: 80%;
  background: radial-gradient(ellipse, rgb(45 91 255 / 0.35), transparent 70%);
  pointer-events: none;
}
.hero-inner { position: relative; z-index: 1; padding: 2rem 2.25rem; }
.hero-head { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1rem; }
.hero h1 { margin: 0 0 1.5rem; font-size: clamp(1.5rem, 4vw, 2rem); line-height: 1.3; letter-spacing: -0.02em; }
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: rgb(255 255 255 / 0.1);
  border-radius: 10px;
  backdrop-filter: blur(4px);
}
.info-item .el-icon { font-size: 1.25rem; margin-top: 2px; opacity: 0.9; }
.info-label { display: block; font-size: 0.75rem; opacity: 0.75; margin-bottom: 0.2rem; }
.info-value { display: block; font-size: 0.9375rem; font-weight: 600; }
.feature-row { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.feature-row .el-tag .el-icon { margin-right: 4px; vertical-align: -2px; }
.body { display: grid; grid-template-columns: 280px 1fr; gap: 1.5rem; align-items: start; }
.summary {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.25rem 1.5rem;
  position: sticky;
  top: 1rem;
}
.summary h3 { margin: 0 0 1rem; font-size: 1rem; color: var(--muted); font-weight: 600; }
.summary dl { margin: 0; }
.summary dt { font-size: 0.75rem; color: var(--muted); margin-bottom: 0.2rem; }
.summary dd { margin: 0 0 1rem; font-size: 0.9375rem; line-height: 1.5; }
.service-tags { display: flex; flex-wrap: wrap; gap: 0.35rem; }
.service-tags span {
  font-size: 0.8125rem;
  padding: 0.15rem 0.5rem;
  background: var(--surface);
  border-radius: 4px;
  color: var(--primary);
}
.form-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.75rem 2rem;
}
.form-header { margin-bottom: 1.25rem; }
.form-header h2 { margin: 0 0 0.35rem; font-size: 1.25rem; }
.form-header p { margin: 0; color: var(--muted); font-size: 0.875rem; }
.alert { margin-bottom: 1rem; }
.submit-btn { width: 100%; margin-top: 0.5rem; height: 42px; }
.footer-note { margin-top: 1.25rem; text-align: center; font-size: 0.875rem; color: var(--muted); }
.link { color: var(--primary); text-decoration: none; }
@media (max-width: 768px) {
  .body { grid-template-columns: 1fr; }
  .summary { position: static; order: -1; }
  .hero-inner { padding: 1.5rem; }
  .info-grid { grid-template-columns: 1fr 1fr; }
  .form-card { padding: 1.25rem; }
}
@media (max-width: 420px) {
  .info-grid { grid-template-columns: 1fr; }
}
</style>
