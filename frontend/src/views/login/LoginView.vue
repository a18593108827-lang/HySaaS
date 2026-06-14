<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const registerEventId = computed(() => {
  const fromQuery = route.query.eventId
  if (typeof fromQuery === 'string' && fromQuery) return fromQuery
  const redirect = route.query.redirect
  if (typeof redirect === 'string') {
    const m = redirect.match(/\/event\/([^/?]+)\/register/) || redirect.match(/\/portal\/events\/([^/?]+)\/register/)
    if (m) return m[1]
  }
  return ''
})

const account = ref('')
const password = ref('')
const remember = ref(true)
const showPw = ref(false)
const banner = ref('')
const accountError = ref(false)
const passwordError = ref(false)

const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phoneRe = /^1\d{10}$/

function validAccount(v: string) {
  return emailRe.test(v) || phoneRe.test(v)
}

async function onSubmit() {
  banner.value = ''
  accountError.value = !validAccount(account.value.trim())
  passwordError.value = password.value.length < 6
  if (accountError.value || passwordError.value) return

  try {
    await auth.login({ username: account.value.trim(), password: password.value })
    const redirect = (route.query.redirect as string) || auth.homePath
    router.push(redirect)
  } catch (e: unknown) {
    banner.value = (e as { message?: string })?.message || '账号或密码不正确'
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
        <h1>会议从这里开始</h1>
        <p>报名、投稿、签到与酒店预订，同一套账号贯穿你的参会流程。</p>
      </div>
    </aside>

    <main class="main">
      <div class="card">
        <header class="card-header">
          <h2>登录账号</h2>
          <p>使用企业邮箱或手机号进入工作台</p>
        </header>

        <div v-if="banner" class="banner-error" role="alert">{{ banner }}</div>

        <form @submit.prevent="onSubmit">
          <div class="field">
            <label for="account">邮箱或手机号</label>
            <el-input
              id="account"
              v-model="account"
              autocomplete="username"
              placeholder="name@company.com"
              :aria-invalid="accountError"
            />
            <p v-if="accountError" class="field-error">请输入有效的邮箱或手机号</p>
          </div>

          <div class="field">
            <label for="password">密码</label>
            <el-input
              id="password"
              v-model="password"
              :type="showPw ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="输入密码"
              :aria-invalid="passwordError"
            >
              <template #suffix>
                <el-button link type="primary" @click="showPw = !showPw">
                  {{ showPw ? '隐藏' : '显示' }}
                </el-button>
              </template>
            </el-input>
            <p v-if="passwordError" class="field-error">密码至少 6 位</p>
          </div>

          <div class="row">
            <el-checkbox v-model="remember">保持登录</el-checkbox>
            <a class="link" href="#">找回密码</a>
          </div>

          <el-button type="primary" native-type="submit" class="submit-btn" :loading="auth.loading">
            登录
          </el-button>
        </form>

        <p class="footer-note">
          还没有账号？
          <router-link v-if="registerEventId" class="link" :to="`/event/${registerEventId}/register`">活动报名</router-link>
          <template v-else><router-link class="link" to="/register">企业入驻申请</router-link></template>
        </p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  min-height: 100dvh;
}

.brand {
  flex: 0 0 42%;
  max-width: 520px;
  background: linear-gradient(160deg, var(--navy) 0%, var(--navy-mid) 55%, #122d8a 100%);
  color: var(--panel-ink);
  padding: clamp(2rem, 5vw, 3.5rem);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.brand::before {
  content: '';
  position: absolute;
  inset: -20% auto auto -10%;
  width: 70%;
  height: 50%;
  background: radial-gradient(ellipse at 30% 30%, rgb(45 91 255 / 0.35), transparent 70%);
  pointer-events: none;
}

.brand::after {
  content: '';
  position: absolute;
  inset: auto -20% -30% -10%;
  height: 55%;
  background: radial-gradient(ellipse at 30% 80%, rgb(45 91 255 / 0.28), transparent 70%);
  pointer-events: none;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 700;
  font-size: 1.125rem;
  letter-spacing: -0.02em;
  position: relative;
  z-index: 1;
}

.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--primary);
  display: grid;
  place-items: center;
  box-shadow: 0 4px 16px rgb(45 91 255 / 0.4);
}

.logo-mark svg {
  width: 20px;
  height: 20px;
}

.brand-main {
  position: relative;
  z-index: 1;
  max-width: 32ch;
}

.brand-main h1 {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.03em;
  text-wrap: balance;
  margin: 0 0 1rem;
}

.brand-main p {
  font-size: 1rem;
  color: var(--panel-muted);
  text-wrap: pretty;
  max-width: 28ch;
  margin: 0;
}

.main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1.5rem, 4vw, 3rem);
  background: var(--surface);
}

.card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-soft);
}

.card-header {
  margin-bottom: 2rem;
}

.card-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  text-wrap: balance;
  margin: 0 0 0.35rem;
}

.card-header p {
  font-size: 0.9375rem;
  color: var(--muted);
  margin: 0;
}

.field {
  margin-bottom: 1.25rem;
}

.field label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.4rem;
}

.field-error {
  font-size: 0.8125rem;
  color: var(--error);
  margin: 0.35rem 0 0;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
}

.link {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  color: var(--primary-hover);
}

.submit-btn {
  width: 100%;
  height: 44px;
}

.banner-error {
  padding: 0.75rem 1rem;
  margin-bottom: 1.25rem;
  font-size: 0.875rem;
  color: var(--error);
  background: var(--error-bg);
  border-radius: var(--radius);
}

.footer-note {
  margin-top: 1.75rem;
  text-align: center;
  font-size: 0.875rem;
  color: var(--muted);
}

@media (max-width: 860px) {
  .shell {
    flex-direction: column;
  }
  .brand {
    flex: none;
    max-width: none;
    min-height: auto;
    padding: 1.75rem 1.5rem 2rem;
  }
  .brand-main h1 {
    font-size: 1.5rem;
  }
}
</style>
