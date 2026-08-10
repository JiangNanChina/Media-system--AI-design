<template>
  <div class="maintenance-page">
    <router-link to="/" class="public-link"><el-icon><ArrowLeft /></el-icon>浏览公开站点</router-link>
    <main>
      <div class="status-icon"><el-icon><Tools /></el-icon></div>
      <span>MAINTENANCE ACCESS</span>
      <h1>{{ status.title || '系统维护中' }}</h1>
      <p>{{ status.message || '管理平台正在维护，公开站点仍可正常浏览。' }}</p>
      <el-alert title="维护密码仅提供临时通行，进入后仍需使用个人账号登录。" type="info" :closable="false" show-icon />
      <el-form @submit.prevent="unlock">
        <el-input v-model="password" type="password" show-password size="large" placeholder="输入维护密码" @keyup.enter="unlock"><template #prefix><el-icon><Lock /></el-icon></template></el-input>
        <el-button type="primary" size="large" :loading="loading" :disabled="password.length < 8" @click="unlock">验证并继续</el-button>
      </el-form>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Lock, Tools } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute(); const router = useRouter()
const status = reactive({ enabled: true, title: '', message: '', unlocked: false })
const password = ref(''); const loading = ref(false)
const target = () => typeof route.query.redirect === 'string' ? route.query.redirect : '/login'
const unlock = async () => {
  loading.value = true
  try { await request.post('/maintenance/public/unlock', { password: password.value }, { skipAuthRefresh: true }); ElMessage.success('维护通行验证成功'); router.replace(target()) }
  finally { loading.value = false }
}
onMounted(async () => {
  const response = await request.get('/maintenance/public/status', { silent: true })
  Object.assign(status, response.data || {})
  if (!status.enabled || status.unlocked) router.replace(target())
})
</script>

<style scoped>
.maintenance-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; color: #173747; background: #0c3c50; position: relative; z-index: 2; }
.public-link { position: fixed; top: 28px; left: 30px; display: flex; align-items: center; gap: 7px; color: #d3edf3; text-decoration: none; }
main { width: min(520px, 100%); padding: 38px; background: white; border: 1px solid #cfe2e8; border-radius: 6px; box-shadow: 0 22px 60px rgba(0,19,28,.3); }
.status-icon { width: 52px; height: 52px; display: grid; place-items: center; background: #e5f5f8; color: #087da8; border-radius: 6px; font-size: 26px; }
main > span { display: block; margin-top: 22px; color: #087da8; font-size: 12px; font-weight: 700; }
h1 { font-size: 34px; margin: 7px 0 12px; letter-spacing: 0; }
main > p { color: #5a7482; line-height: 1.75; margin-bottom: 22px; }
form { margin-top: 22px; display: grid; grid-template-columns: 1fr auto; gap: 10px; }
@media(max-width: 560px) { main { padding: 30px 20px; } form { grid-template-columns: 1fr; } .public-link { top: 18px; left: 18px; } }
</style>
