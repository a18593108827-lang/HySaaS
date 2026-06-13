<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGlobalConfig, updateGlobalConfig } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const form = ref({
  smtpHost: '',
  smtpPort: '465',
  smtpUser: '',
  smtpPass: '',
  alipayAppId: '',
  alipayPrivateKey: '',
  invoiceAppKey: '',
  invoiceAppSecret: '',
})

async function load() {
  loading.value = true
  try {
    const data = await getGlobalConfig()
    if (data) Object.assign(form.value, data)
  } catch {
    /* demo defaults */
  } finally {
    loading.value = false
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

onMounted(load)
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

      <el-divider content-position="left">支付宝</el-divider>
      <el-form-item label="App ID">
        <el-input v-model="form.alipayAppId" />
      </el-form-item>
      <el-form-item label="私钥">
        <el-input v-model="form.alipayPrivateKey" type="textarea" :rows="3" />
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
  </div>
</template>
