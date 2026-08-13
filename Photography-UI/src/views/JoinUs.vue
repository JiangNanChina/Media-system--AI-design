<template>
  <div class="join-page">
    <header class="join-nav">
      <router-link to="/" class="nav-link">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </router-link>
      <strong>加入我们</strong>
      <router-link to="/login" class="nav-link nav-link-right">
        <el-icon><Monitor /></el-icon>
        <span>管理平台</span>
      </router-link>
    </header>

    <main class="join-main">
      <section class="join-layout" aria-label="入部申请表单">
        <aside class="join-panel">
          <div class="panel-visual">
            <img :src="logoUrl" alt="融媒体中心标志" />
          </div>
          <div class="panel-copy">
            <span>JOIN THE TEAM</span>
            <h1>提交入部申请</h1>
            <p>填写基本信息和自我介绍，作品可选上传。申请通过后，系统会通过 QQ 邮箱发送面试群通知。</p>
          </div>
          <div class="panel-steps">
            <div>
              <strong>01</strong>
              <span>填写资料</span>
            </div>
            <div>
              <strong>02</strong>
              <span>等待审核</span>
            </div>
            <div>
              <strong>03</strong>
              <span>进入面试</span>
            </div>
          </div>
        </aside>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="join-form">
          <div class="form-title">
            <div>
              <span>Application Form</span>
              <h2>申请信息</h2>
            </div>
            <em>带 * 为必填项</em>
          </div>

          <div class="form-grid">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model.trim="form.realName" maxlength="80" size="large" placeholder="请输入真实姓名">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="QQ邮箱" prop="qqEmail">
              <el-input v-model.trim="form.qqEmail" type="email" size="large" placeholder="example@qq.com">
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" maxlength="11" size="large" placeholder="请输入手机号">
                <template #prefix><el-icon><Phone /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender" class="gender-group">
                <el-radio-button label="MALE">男</el-radio-button>
                <el-radio-button label="FEMALE">女</el-radio-button>
                <el-radio-button label="OTHER">其他</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="学院" prop="college">
              <el-select
                v-model="form.college"
                size="large"
                placeholder="请选择学院"
                filterable
                clearable
                no-data-text="暂无学院，请联系管理员添加"
              >
                <template #prefix><el-icon><School /></el-icon></template>
                <el-option
                  v-for="college in colleges"
                  :key="college.id"
                  :label="college.name"
                  :value="college.name"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="专业" prop="major">
              <el-input v-model.trim="form.major" maxlength="160" size="large" placeholder="请输入专业">
                <template #prefix><el-icon><Reading /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item label="入学年份" prop="enrollmentYear">
              <el-select v-model="form.enrollmentYear" size="large" placeholder="请选择入学年份">
                <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="自我介绍" prop="selfIntroduction" class="wide-item">
            <el-input
              v-model="form.selfIntroduction"
              type="textarea"
              :rows="6"
              maxlength="5000"
              show-word-limit
              placeholder="可以介绍你的兴趣方向、过往经历、想加入融媒体中心的原因"
            />
          </el-form-item>

          <el-form-item label="自我作品（选填）" class="wide-item">
            <el-upload
              drag
              :auto-upload="false"
              :limit="1"
              accept=".jpg,.jpeg,.png,.gif,.bmp,.webp,.mp4,.mov,.webm,image/jpeg,image/png,image/gif,image/bmp,image/webp,video/mp4,video/quicktime,video/webm"
              :on-change="selectWork"
              :on-remove="removeWork"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽图片或视频到此处，或点击选择</div>
              <template #tip>
                <span>支持常见图片和 MP4、MOV、WebM，最大 300MB。</span>
              </template>
            </el-upload>
            <div v-if="workMeta" class="selected-file">
              <el-icon><Files /></el-icon>
              <span>{{ workMeta.name }}</span>
              <strong>{{ workMeta.size }}</strong>
            </div>
          </el-form-item>

          <label class="consent-row">
            <el-checkbox v-model="consent" />
            <span>我确认以上信息真实有效，并同意融媒体中心通过 QQ 邮箱或手机号联系我。</span>
          </label>

          <div v-if="submitting" class="progress" aria-live="polite">
            <el-progress :percentage="progress" :stroke-width="10" />
            <span>正在提交申请，请不要关闭页面</span>
          </div>

          <el-button class="submit-button" type="primary" size="large" :loading="submitting" :disabled="!consent" @click="submit">
            <el-icon><Promotion /></el-icon>
            <span>提交申请</span>
          </el-button>
        </el-form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Files,
  Message,
  Monitor,
  Phone,
  Promotion,
  Reading,
  School,
  UploadFilled,
  User
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const formRef = ref()
const consent = ref(false)
const work = ref(null)
const colleges = ref([])
const submitting = ref(false)
const progress = ref(0)
const siteLogo = ref('')
const logoUrl = computed(() => siteLogo.value || '/logo.svg')
let progressTimer

const currentYear = new Date().getFullYear()
const yearOptions = computed(() => {
  const start = currentYear + 1
  return Array.from({ length: 10 }, (_, index) => start - index)
})

const form = reactive({
  realName: '',
  qqEmail: '',
  phone: '',
  gender: '',
  college: '',
  major: '',
  enrollmentYear: currentYear,
  selfIntroduction: ''
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  qqEmail: [
    { required: true, message: '请输入QQ邮箱', trigger: 'blur' },
    { pattern: /^[^@\s]+@qq\.com$/, message: '请输入有效QQ邮箱', trigger: 'blur' }
  ],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入有效手机号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  enrollmentYear: [{ required: true, type: 'number', message: '请选择入学年份', trigger: 'change' }],
  selfIntroduction: [{ required: true, message: '请输入自我介绍', trigger: 'blur' }]
}

const formatFileSize = bytes => {
  if (!bytes) return '0 MB'
  const mb = bytes / 1024 / 1024
  return mb >= 10 ? `${Math.round(mb)} MB` : `${mb.toFixed(1)} MB`
}

const workMeta = computed(() => work.value ? {
  name: work.value.name,
  size: formatFileSize(work.value.size)
} : null)

const selectWork = uploadFile => {
  if (uploadFile.raw.size > 300 * 1024 * 1024) {
    ElMessage.error('作品文件不能超过300MB')
    work.value = null
    return
  }
  if (!/\.(jpg|jpeg|png|gif|bmp|webp|mp4|mov|webm)$/i.test(uploadFile.name)) {
    ElMessage.error('仅支持图片和 MP4、MOV、WebM 视频')
    work.value = null
    return
  }
  work.value = uploadFile.raw
}

const removeWork = () => {
  work.value = null
}

const fetchColleges = async () => {
  try {
    const response = await request.get('/colleges/list', { silent: true })
    colleges.value = response.data || []
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

const fetchSiteConfig = async () => {
  try {
    const response = await request.get('/site-config/public', { silent: true })
    siteLogo.value = getSiteImageUrl(response.data?.['site.logo']) || ''
  } catch (error) {
    console.error('获取站点配置失败:', error)
  }
}

const submit = async () => {
  await formRef.value?.validate()
  if (!consent.value) {
    ElMessage.warning('请先确认信息授权')
    return
  }

  const data = new FormData()
  Object.entries(form).forEach(([key, value]) => data.append(key, value ?? ''))
  if (work.value) data.append('work', work.value)

  submitting.value = true
  progress.value = 0
  progressTimer = setInterval(() => {
    progress.value = Math.min(progress.value + 8, 96)
  }, 220)
  try {
    const response = await request.post('/join-applications/public', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 10 * 60 * 1000,
      onUploadProgress: event => {
        if (event.total) progress.value = Math.round(event.loaded * 100 / event.total)
      }
    })
    progress.value = 100
    await ElMessageBox.alert(`申请编号：${response.data.applicationNumber}`, '申请已提交', {
      confirmButtonText: '返回首页'
    })
    router.push('/')
  } finally {
    submitting.value = false
    clearInterval(progressTimer)
  }
}

onMounted(async () => {
  fetchColleges()
  fetchSiteConfig()
  const status = await request.get('/maintenance/public/status', { silent: true })
  if (status.data?.enabled && !status.data?.unlocked) router.replace({ path: '/maintenance', query: { redirect: '/join-us' } })
})

onBeforeUnmount(() => clearInterval(progressTimer))
</script>

<style scoped>
.join-page {
  min-height: 100vh;
  position: relative;
  z-index: 2;
  color: #173f56;
  background:
    linear-gradient(rgba(18, 174, 231, 0.055) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.055) 1px, transparent 1px),
    linear-gradient(135deg, #fbfdff 0%, #eef8ff 48%, #fffaf0 100%);
  background-size: 34px 34px, 34px 34px, auto;
  overflow-x: hidden;
}

.join-nav {
  position: sticky;
  top: 0;
  z-index: 10;
  min-height: 68px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  padding: 12px max(18px, calc((100% - 1180px) / 2));
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
  box-shadow: 0 14px 34px rgba(18, 174, 231, 0.08);
  backdrop-filter: blur(18px) saturate(1.1);
  -webkit-backdrop-filter: blur(18px) saturate(1.1);
}

.join-nav strong {
  color: #12384f;
  font-size: 15px;
  font-weight: 850;
}

.nav-link {
  width: fit-content;
  min-height: 40px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 0 14px;
  color: #087fc4;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(98, 177, 210, 0.22);
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(18, 174, 231, 0.1);
}

.nav-link-right {
  justify-self: end;
}

.nav-link:hover,
.nav-link:focus-visible {
  color: #075985;
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(37, 184, 242, 0.42);
}

.join-main {
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: clamp(30px, 5vw, 58px) 0 76px;
}

.join-layout {
  display: grid;
  grid-template-columns: minmax(280px, 0.72fr) minmax(0, 1.28fr);
  gap: 24px;
  align-items: start;
}

.join-panel,
.join-form {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  box-shadow: 0 24px 62px rgba(18, 85, 116, 0.1);
  backdrop-filter: blur(24px) saturate(1.08);
  -webkit-backdrop-filter: blur(24px) saturate(1.08);
}

.join-panel {
  position: sticky;
  top: 92px;
  overflow: hidden;
  padding: 26px;
}

.panel-visual {
  min-height: 220px;
  display: grid;
  place-items: center;
  margin: -8px -8px 24px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.14) 1px, transparent 1px),
    linear-gradient(145deg, #0b3a4f 0%, #0c6f8e 58%, #f59e0b 100%);
  background-size: 26px 26px, 26px 26px, auto;
  border-radius: 22px;
}

.panel-visual img {
  width: 96px;
  height: 96px;
  object-fit: contain;
  padding: 18px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  box-shadow: 0 20px 42px rgba(0, 18, 30, 0.22);
}

.panel-copy span,
.form-title span {
  display: block;
  margin-bottom: 6px;
  color: #087fc4;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.panel-copy h1,
.form-title h2 {
  margin: 0;
  color: #12384f;
  font-weight: 900;
  letter-spacing: 0;
}

.panel-copy h1 {
  font-size: 34px;
  line-height: 1.15;
}

.panel-copy p {
  margin: 14px 0 0;
  color: #4f6d82;
  font-size: 15px;
  line-height: 1.8;
}

.panel-steps {
  display: grid;
  gap: 12px;
  margin-top: 24px;
}

.panel-steps div {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  background: rgba(245, 251, 255, 0.84);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 16px;
}

.panel-steps strong {
  display: grid;
  place-items: center;
  height: 32px;
  color: #a64d0a;
  font-size: 12px;
  background: #fff7ed;
  border-radius: 999px;
}

.panel-steps span {
  color: #31556e;
  font-size: 14px;
  font-weight: 850;
}

.join-form {
  padding: clamp(24px, 3vw, 34px);
}

.form-title {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.form-title h2 {
  font-size: 22px;
}

.form-title em {
  color: #7f9ab0;
  font-size: 13px;
  font-style: normal;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.join-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.join-form :deep(.el-form-item__label) {
  color: #31556e !important;
  font-size: 13px !important;
  font-weight: 850 !important;
  line-height: 1.2 !important;
  margin-bottom: 8px !important;
}

.join-form :deep(.el-input__wrapper),
.join-form :deep(.el-select__wrapper),
.join-form :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.94) !important;
  border: 1px solid rgba(98, 177, 210, 0.22) !important;
  border-radius: 14px !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86) !important;
}

.join-form :deep(.el-select) {
  width: 100%;
}

.join-form :deep(.el-input__wrapper),
.join-form :deep(.el-select__wrapper) {
  min-height: 46px;
}

.join-form :deep(.el-textarea__inner) {
  min-height: 140px !important;
  padding: 14px 16px !important;
  resize: vertical;
}

.gender-group {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.gender-group :deep(.el-radio-button__inner) {
  width: 100%;
  min-height: 46px;
  display: grid;
  place-items: center;
  border-color: rgba(98, 177, 210, 0.22);
  font-weight: 800;
}

.wide-item {
  grid-column: 1 / -1;
}

.join-form :deep(.el-upload),
.join-form :deep(.el-upload-dragger) {
  width: 100%;
}

.join-form :deep(.el-upload-dragger) {
  min-height: 176px;
  display: grid;
  place-items: center;
  padding: 28px 20px;
  background:
    linear-gradient(rgba(18, 174, 231, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.09) 1px, transparent 1px),
    linear-gradient(135deg, rgba(255, 255, 255, 0.86), rgba(231, 250, 255, 0.62));
  background-size: 28px 28px, 28px 28px, auto;
  border: 1px dashed rgba(37, 184, 242, 0.48) !important;
  border-radius: 20px !important;
}

.upload-icon {
  margin-bottom: 10px;
  color: #087fc4;
  font-size: 42px;
}

.selected-file {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 12px 14px;
  color: #12384f;
  background: rgba(245, 251, 255, 0.84);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 14px;
}

.selected-file span {
  min-width: 0;
  overflow: hidden;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-file strong {
  color: #5f788b;
  font-size: 12px;
}

.consent-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: start;
  gap: 10px;
  margin-top: 4px;
  color: #4f6d82;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.6;
  cursor: pointer;
}

.progress {
  display: grid;
  gap: 8px;
  margin-top: 20px;
  padding: 14px;
  color: #5f788b;
  font-size: 13px;
  font-weight: 700;
  background: rgba(245, 251, 255, 0.84);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 14px;
}

.submit-button {
  width: 100%;
  height: 52px;
  margin-top: 24px;
  color: #fff !important;
  font-size: 16px;
  font-weight: 900;
  background: linear-gradient(135deg, #f97316, #0ea5c6) !important;
  border: 0 !important;
  border-radius: 14px !important;
  box-shadow: 0 16px 32px rgba(14, 165, 198, 0.2) !important;
}

@media (max-width: 980px) {
  .join-layout {
    grid-template-columns: 1fr;
  }

  .join-panel {
    position: static;
  }
}

@media (max-width: 680px) {
  .join-nav {
    grid-template-columns: 1fr auto;
  }

  .join-nav strong {
    display: none;
  }

  .join-main {
    width: min(100% - 24px, 560px);
    padding: 24px 0 46px;
  }

  .join-panel,
  .join-form {
    border-radius: 24px;
    padding: 22px 18px;
  }

  .panel-visual {
    min-height: 170px;
  }

  .form-grid,
  .gender-group {
    grid-template-columns: 1fr;
  }

  .form-title {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .selected-file {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .selected-file strong {
    grid-column: 2;
  }
}
</style>
