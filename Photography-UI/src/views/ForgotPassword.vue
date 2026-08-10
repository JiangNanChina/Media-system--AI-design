<template>
  <div class="auth-simple-page">
    <router-link to="/" class="home-link"><el-icon><ArrowLeft /></el-icon>返回公开站点</router-link>
    <main>
      <div class="page-mark"><el-icon><Lock /></el-icon></div>
      <h1>找回密码</h1>
      <p>通过账号绑定的 QQ 邮箱验证身份，重置后所有已登录设备将退出。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="QQ邮箱" prop="email"><el-input v-model.trim="form.email" type="email" size="large" /></el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row"><el-input v-model.trim="form.code" maxlength="6" size="large" /><el-button size="large" :disabled="countdown > 0" :loading="sending" @click="sendCode">{{ countdown ? `${countdown}s` : '发送验证码' }}</el-button></div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password size="large" /></el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password size="large" /></el-form-item>
        <el-button type="primary" size="large" class="full-button" :loading="submitting" @click="submit">重置密码</el-button>
      </el-form>
      <router-link to="/login" class="login-link">返回登录</router-link>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const sending = ref(false)
const submitting = ref(false)
const countdown = ref(0)
let timer
const form = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const rules = {
  email: [{ required: true, type: 'email', message: '请输入有效QQ邮箱', trigger: 'blur' }],
  code: [{ required: true, pattern: /^\d{6}$/, message: '请输入6位验证码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 8, max: 72, message: '密码长度为8-72位', trigger: 'blur' }],
  confirmPassword: [{ validator: (_r, value, callback) => value === form.newPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur' }]
}
const sendCode = async () => {
  await formRef.value.validateField('email')
  sending.value = true
  try {
    await request.post('/auth/password-reset/email-code', { email: form.email })
    ElMessage.success('如邮箱已注册，验证码将发送到邮箱')
    countdown.value = 60
    timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
  } finally { sending.value = false }
}
const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await request.post('/auth/password-reset', { email: form.email, code: form.code, newPassword: form.newPassword })
    ElMessage.success('密码已重置')
    router.push('/login')
  } finally { submitting.value = false }
}
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.auth-simple-page { min-height: 100vh; padding: 28px; display: grid; place-items: center; background: #eef6f8; position: relative; z-index: 2; }
.home-link { position: fixed; top: 26px; left: 28px; display: flex; gap: 6px; align-items: center; color: #197b9e; text-decoration: none; }
main { width: min(460px, 100%); padding: 36px; background: white; border: 1px solid #d9e7ec; border-radius: 6px; box-shadow: 0 16px 40px rgba(15,65,84,.1); }
.page-mark { width: 48px; height: 48px; display: grid; place-items: center; color: #087da8; font-size: 24px; background: #e5f5f8; border-radius: 6px; }
h1 { font-size: 30px; margin: 18px 0 8px; letter-spacing: 0; }
p { color: #637b87; line-height: 1.7; margin-bottom: 26px; }
.code-row { width: 100%; display: grid; grid-template-columns: 1fr auto; gap: 10px; }
.full-button { width: 100%; }
.login-link { display: block; text-align: center; margin-top: 22px; color: #197b9e; }
@media(max-width: 520px) { .auth-simple-page { padding: 18px; } main { padding: 28px 20px; } .home-link { position: absolute; top: 18px; left: 18px; } }
</style>
