<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGlobalConfig, updateGlobalConfig, testSmtpEmail, getAdminEmailLogs } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const testTo = ref('')
const logLoading = ref(false)
const logs = ref<{ id: number; code: string; recipient: string; subject: string; status: string; errorMsg?: string; retryCount: number; createdAt: string }[]>([])
const form = ref({
  smtpHost: '',
  smtpPort: '465',
  smtpUser: '',
  smtpPass: '',
  alipayAppId: '',
  alipayPrivateKey: '',
  alipayPublicKey: '',
  alipayNotifyUrl: '',
  alipayReturnUrl: '',
  invoiceAppKey: '',
  invoiceAppSecret: '',
})

async function load() {
  loading.value = true
  try {
    const data = await getGlobalConfig()
    if (data) Object.assign(form.value, data)
  } catch {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const res = await getAdminEmailLogs({ page: 0, size: 20 })
    logs.value = res.records
  } catch {
    logs.value = []
    ElMessage.error('加载发信记录失败')
  } finally {
    logLoading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await updateGlobalConfig(form.value)
    ElMessage.success('配置已保存')
  } catch {
    return
  } finally {
    saving.value = false
  }
}

async function testEmail() {
  if (!testTo.value) {
    ElMessage.warning('请输入测试邮箱')
    return
  }
  testing.value = true
  try {
    await testSmtpEmail(testTo.value)
    ElMessage.success('测试邮件已发送')
    loadLogs()
  } catch {
    return
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  load()
  loadLogs()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h1>全局配置</h1>
      <p>SMTP、支付宝、票点云等平台级密钥</p>
    </div>
    <el-form v-loading="loading" label-width="120px" style="max-width: 560px">
      <el-divider content-position="left">邮件 SMTP</el-divider>
      <el-form-item label="服务器">
        <el-input v-model="form.smtpHost" placeholder="smtp.example.com" />
      </el-form-item>
      <el-form-item label="端口">
        <el-input v-model="form.smtpPort" />
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="form.smtpUser" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.smtpPass" type="password" show-password />
      </el-form-item>
      <el-form-item label="测试邮箱">
        <el-input v-model="testTo" placeholder="收件地址" style="width: 220px; margin-right: 8px" />
        <el-button :loading="testing" @click="testEmail">发送测试</el-button>
      </el-form-item>

      <el-divider content-position="left">支付宝</el-divider>
      <el-form-item label="App ID">
        <el-input v-model="form.alipayAppId" placeholder="开放平台应用 APPID" />
      </el-form-item>
      <el-form-item label="应用私钥">
        <el-input v-model="form.alipayPrivateKey" type="textarea" :rows="3" placeholder="RSA2 应用私钥" />
      </el-form-item>
      <el-form-item label="支付宝公钥">
        <el-input v-model="form.alipayPublicKey" type="textarea" :rows="3" placeholder="开放平台「支付宝公钥」" />
      </el-form-item>
      <el-form-item label="异步通知 URL">
        <el-input v-model="form.alipayNotifyUrl" placeholder="留空则用 hysaas.pay.public-base-url + /pay/alipay/notify" />
      </el-form-item>
      <el-form-item label="同步跳转 URL">
        <el-input v-model="form.alipayReturnUrl" placeholder="支付完成后跳回，如 https://域名/portal/orders" />
      </el-form-item>

      <el-divider content-position="left">票点云</el-divider>
      <el-form-item label="App Key">
        <el-input v-model="form.invoiceAppKey" />
      </el-form-item>
      <el-form-item label="App Secret">
        <el-input v-model="form.invoiceAppSecret" type="password" show-password />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">发信记录</el-divider>
    <el-table v-loading="logLoading" :data="logs" border stripe style="max-width: 960px">
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="recipient" label="收件人" min-width="160" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="errorMsg" label="错误" min-width="180" show-overflow-tooltip />
    </el-table>
  </div>
</template>
