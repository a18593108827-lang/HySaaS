<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyTenant } from '@/api/public'

const router = useRouter()
const submitting = ref(false)
const done = ref(false)
const form = ref({
  name: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  remark: '',
})

const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function onSubmit() {
  if (!form.value.name || !form.value.contactName || !form.value.contactPhone || !form.value.contactEmail) {
    return ElMessage.warning('请填写必填项')
  }
  if (!emailRe.test(form.value.contactEmail.trim())) {
    return ElMessage.warning('联系邮箱格式不正确')
  }
  submitting.value = true
  try {
    await applyTenant(form.value)
    done.value = true
    ElMessage.success('申请已提交，请等待平台审核')
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
        <h1>企业入驻</h1>
        <p>提交会务管理申请，审核通过后即可使用企业端。</p>
      </div>
    </aside>

    <main class="main">
      <div class="card">
        <el-result v-if="done" icon="success" title="申请已提交" sub-title="平台将在 1–3 个工作日内完成审核">
          <template #extra>
            <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
          </template>
        </el-result>

        <template v-else>
          <header class="card-header">
            <h2>入驻申请</h2>
            <p>填写企业信息，提交后进入待审核状态</p>
          </header>

          <form @submit.prevent="onSubmit">
            <div class="field">
              <label>企业名称 *</label>
              <el-input v-model="form.name" placeholder="如：华东医学会" />
            </div>
            <div class="field">
              <label>联系人 *</label>
              <el-input v-model="form.contactName" />
            </div>
            <div class="field">
              <label>联系电话 *</label>
              <el-input v-model="form.contactPhone" />
            </div>
            <div class="field">
              <label>联系邮箱 *</label>
              <el-input v-model="form.contactEmail" type="email" placeholder="name@company.com" />
            </div>
            <div class="field">
              <label>企业地址</label>
              <el-input v-model="form.address" />
            </div>
            <div class="field">
              <label>备注</label>
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="主营业务、预计会议规模等" />
            </div>
            <el-button type="primary" native-type="submit" class="submit-btn" :loading="submitting">
              提交申请
            </el-button>
          </form>
        </template>

        <p v-if="!done" class="footer-note">已有账号？<router-link class="link" to="/login">去登录</router-link></p>
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
  background: var(--panel);
  color: var(--panel-ink);
  padding: clamp(2rem, 5vw, 3.5rem);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 700;
  font-size: 1.125rem;
}

.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: var(--primary);
  display: grid;
  place-items: center;
}

.logo-mark svg {
  width: 20px;
  height: 20px;
}

.brand-main h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 1rem;
}

.brand-main p {
  color: var(--panel-muted);
  margin: 0;
}

.main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1.5rem, 4vw, 3rem);
}

.card {
  width: 100%;
  max-width: 440px;
}

.card-header {
  margin-bottom: 1.5rem;
}

.card-header h2 {
  margin: 0 0 0.35rem;
  font-size: 1.5rem;
  font-weight: 700;
}

.card-header p {
  margin: 0;
  font-size: 0.9375rem;
  color: var(--muted);
}

.field {
  margin-bottom: 1rem;
}

.field label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.4rem;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 0.5rem;
}

.footer-note {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.875rem;
  color: var(--muted);
}

.link {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

@media (max-width: 860px) {
  .shell {
    flex-direction: column;
  }
  .brand {
    flex: none;
    max-width: none;
  }
}
</style>
