<template>
  <div class="submission-page">
    <header class="submission-nav">
      <router-link to="/" class="nav-link">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </router-link>
      <strong>视频投稿</strong>
      <router-link to="/login" class="nav-link nav-link-right">
        <el-icon><Monitor /></el-icon>
        <span>管理平台</span>
      </router-link>
    </header>

    <main class="submission-main">
      <section class="submission-hero">
        <div class="hero-copy">
          <span class="eyebrow">
            <el-icon><VideoCamera /></el-icon>
            VIDEO SUBMISSION
          </span>
          <h1>让校园故事被更多人看见</h1>
          <p>上传视频、留下联系方式，融媒体中心会在私有审核区处理稿件并与你确认后续发布安排。</p>
          <div class="hero-badges" aria-label="投稿要求">
            <span>MP4 / MOV / WebM</span>
            <span>最大 500MB</span>
            <span>邮箱验证后提交</span>
          </div>
        </div>
        <div class="hero-visual" aria-hidden="true">
          <div class="visual-screen">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </section>

      <section class="submission-layout" aria-label="视频投稿表单">
        <aside class="submission-guide">
          <div class="guide-heading">
            <span class="guide-icon"><el-icon><DocumentChecked /></el-icon></span>
            <div>
              <h2>投稿流程</h2>
              <p>三个步骤完成提交</p>
            </div>
          </div>
          <ol class="guide-steps">
            <li>
              <span>01</span>
              <div>
                <strong>填写稿件信息</strong>
                <p>标题、联系人与说明越完整，审核沟通越顺畅。</p>
              </div>
            </li>
            <li>
              <span>02</span>
              <div>
                <strong>验证联系邮箱</strong>
                <p>验证码用于确认投稿人身份和后续通知渠道。</p>
              </div>
            </li>
            <li>
              <span>03</span>
              <div>
                <strong>上传视频文件</strong>
                <p>文件仅进入私有审核区，不会直接公开访问。</p>
              </div>
            </li>
          </ol>
          <div class="privacy-note">
            <el-icon><Lock /></el-icon>
            <span>投稿资料仅用于稿件审核与沟通。</span>
          </div>
        </aside>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="submission-form">
          <div class="section-title">
            <div>
              <span>Submission Form</span>
              <h2>稿件信息</h2>
            </div>
            <em>带 * 为必填项</em>
          </div>

          <div class="form-grid">
            <el-form-item label="投稿标题" prop="title">
              <el-input v-model.trim="form.title" maxlength="160" show-word-limit size="large" placeholder="请输入视频标题">
                <template #prefix><el-icon><EditPen /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="投稿人姓名" prop="submitterName">
              <el-input v-model.trim="form.submitterName" maxlength="80" size="large" placeholder="请输入真实姓名">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" maxlength="11" size="large" placeholder="请输入手机号">
                <template #prefix><el-icon><Phone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="学院或部门">
              <el-input v-model.trim="form.organization" maxlength="160" placeholder="选填" size="large">
                <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="QQ邮箱" prop="email">
              <el-input v-model.trim="form.email" type="email" size="large" placeholder="请输入QQ邮箱">
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="code">
              <div class="code-row">
                <el-input v-model.trim="form.code" maxlength="6" size="large" placeholder="请输入6位验证码">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
                <el-button size="large" :disabled="countdown > 0" :loading="sending" @click="sendCode">
                  <el-icon><Promotion /></el-icon>
                  <span>{{ countdown ? `${countdown}s` : '发送验证码' }}</span>
                </el-button>
              </div>
            </el-form-item>
          </div>

          <el-form-item label="投稿说明" class="description-item">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="5000"
              show-word-limit
              placeholder="可补充拍摄背景、人物信息、活动时间地点或希望呈现的重点"
            />
          </el-form-item>

          <el-form-item label="视频文件" prop="file" class="upload-item">
            <el-upload
              drag
              :auto-upload="false"
              :limit="1"
              accept=".mp4,.mov,.webm,video/mp4,video/quicktime,video/webm"
              :on-change="selectFile"
              :on-remove="removeFile"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽视频到此处，或点击选择</div>
              <template #tip>
                <span>支持 MP4、MOV、WebM，文件会进入私有审核区。</span>
              </template>
            </el-upload>
            <div v-if="fileMeta" class="selected-file">
              <el-icon><VideoCamera /></el-icon>
              <span>{{ fileMeta.name }}</span>
              <strong>{{ fileMeta.size }}</strong>
            </div>
          </el-form-item>

          <label class="consent-row">
            <el-checkbox v-model="consent" />
            <span>我确认拥有投稿内容的使用授权，并同意融媒体中心联系我处理稿件。</span>
          </label>

          <div v-if="uploading" class="progress" aria-live="polite">
            <el-progress :percentage="progress" :stroke-width="10" />
            <span>正在安全上传，请不要关闭页面</span>
          </div>

          <el-button class="submit-button" type="primary" size="large" :loading="uploading" :disabled="!consent" @click="submit">
            <el-icon><UploadFilled /></el-icon>
            <span>提交视频</span>
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
  DocumentChecked,
  EditPen,
  Lock,
  Message,
  Monitor,
  OfficeBuilding,
  Phone,
  Promotion,
  UploadFilled,
  User,
  VideoCamera
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const file = ref(null)
const consent = ref(false)
const sending = ref(false)
const uploading = ref(false)
const progress = ref(0)
const countdown = ref(0)
let timer
const form = reactive({ title: '', submitterName: '', phone: '', organization: '', email: '', code: '', description: '', file: null })
const rules = {
  title: [{ required: true, message: '请输入投稿标题', trigger: 'blur' }],
  submitterName: [{ required: true, message: '请输入投稿人姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入有效手机号', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入有效QQ邮箱', trigger: 'blur' }],
  code: [{ required: true, pattern: /^\d{6}$/, message: '请输入6位验证码', trigger: 'blur' }],
  file: [{ validator: (_rule, _value, callback) => file.value ? callback() : callback(new Error('请选择投稿视频')) }]
}

const formatFileSize = bytes => {
  if (!bytes) return '0 MB'
  const mb = bytes / 1024 / 1024
  return `${mb >= 10 ? Math.round(mb) : mb.toFixed(1)} MB`
}

const fileMeta = computed(() => file.value ? {
  name: file.value.name,
  size: formatFileSize(file.value.size)
} : null)

const selectFile = uploadFile => {
  if (uploadFile.raw.size > 500 * 1024 * 1024) { ElMessage.error('视频不能超过500MB'); file.value = null; return }
  if (!/\.(mp4|mov|webm)$/i.test(uploadFile.name)) { ElMessage.error('仅支持MP4、MOV和WebM'); file.value = null; return }
  file.value = uploadFile.raw
  form.file = uploadFile.raw
  formRef.value?.validateField('file')
}
const removeFile = () => { file.value = null; form.file = null }
const sendCode = async () => {
  await formRef.value?.validateField('email')
  sending.value = true
  try {
    await request.post('/submissions/public/email-code', { email: form.email })
    countdown.value = 60
    timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
    ElMessage.success('验证码已发送')
  } finally { sending.value = false }
}
const submit = async () => {
  await formRef.value?.validate()
  if (!consent.value) return ElMessage.warning('请先确认投稿授权')
  const data = new FormData()
  Object.entries(form).filter(([key]) => key !== 'file').forEach(([key, value]) => data.append(key, value || ''))
  data.append('file', file.value)
  uploading.value = true; progress.value = 0
  try {
    const response = await request.post('/submissions/public', data, {
      headers: { 'Content-Type': 'multipart/form-data' }, timeout: 10 * 60 * 1000,
      onUploadProgress: event => { if (event.total) progress.value = Math.round(event.loaded * 100 / event.total) }
    })
    await ElMessageBox.alert(`投稿编号：${response.data.submissionNumber}`, '投稿成功', { confirmButtonText: '返回首页' })
    router.push('/')
  } finally { uploading.value = false }
}

onMounted(async () => {
  const status = await request.get('/maintenance/public/status', { silent: true })
  if (status.data?.enabled && !status.data?.unlocked) router.replace({ path: '/maintenance', query: { redirect: '/submission' } })
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.submission-page {
  min-height: 100vh;
  position: relative;
  z-index: 2;
  color: #12384f;
  background:
    radial-gradient(circle at 12% 14%, rgba(37, 184, 242, 0.18), transparent 30%),
    radial-gradient(circle at 86% 12%, rgba(98, 214, 189, 0.14), transparent 28%),
    radial-gradient(circle at 80% 88%, rgba(255, 210, 111, 0.12), transparent 30%),
    linear-gradient(135deg, #fbfdff 0%, #edf8ff 48%, #fbfff8 100%);
  overflow-x: hidden;
}

.submission-page::before {
  content: "";
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  background:
    linear-gradient(rgba(18, 174, 231, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.06) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent 82%);
}

.submission-nav {
  position: sticky;
  top: 0;
  z-index: 10;
  min-height: 68px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  padding: 12px max(18px, calc((100% - 1180px) / 2));
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
  box-shadow: 0 14px 34px rgba(18, 174, 231, 0.08);
  backdrop-filter: blur(18px) saturate(1.1);
  -webkit-backdrop-filter: blur(18px) saturate(1.1);
}

.submission-nav strong {
  color: #12384f;
  font-size: 15px;
  font-weight: 850;
  letter-spacing: 0;
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
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(98, 177, 210, 0.22);
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(18, 174, 231, 0.1);
  transition: color 180ms ease, background 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.nav-link-right {
  justify-self: end;
}

.nav-link:hover,
.nav-link:focus-visible {
  color: #075985;
  background: rgba(255, 255, 255, 0.92);
  border-color: rgba(37, 184, 242, 0.42);
  box-shadow: 0 14px 30px rgba(18, 174, 231, 0.16);
}

.submission-main {
  width: min(1180px, calc(100% - 36px));
  margin: 0 auto;
  padding: clamp(34px, 5vw, 60px) 0 76px;
}

.submission-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: stretch;
  gap: 24px;
  margin-bottom: 24px;
  padding: clamp(28px, 4vw, 42px);
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 16%, rgba(37, 184, 242, 0.16), transparent 34%),
    radial-gradient(circle at 88% 78%, rgba(98, 214, 189, 0.16), transparent 36%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.92), rgba(232, 248, 255, 0.76));
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  box-shadow: 0 30px 78px rgba(18, 85, 116, 0.12);
  backdrop-filter: blur(24px) saturate(1.08);
  -webkit-backdrop-filter: blur(24px) saturate(1.08);
}

.hero-copy {
  min-width: 0;
}

.eyebrow {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  color: #087fc4;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.eyebrow::before {
  content: "";
  width: 86px;
  height: 5px;
  border-radius: 999px;
  background: linear-gradient(90deg, #25b8f2, #62d6bd, #ffd26f);
}

.submission-hero h1 {
  max-width: 600px;
  margin: 0 0 14px;
  color: #12384f;
  font-size: clamp(34px, 5vw, 54px);
  font-weight: 900;
  line-height: 1.08;
  letter-spacing: 0;
}

.submission-hero p {
  max-width: 640px;
  margin: 0;
  color: #4f6d82;
  font-size: 16px;
  line-height: 1.7;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.hero-badges span {
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  padding: 0 13px;
  color: #0b6f9f;
  font-size: 13px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 999px;
}

.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-screen {
  width: min(100%, 280px);
  aspect-ratio: 16 / 10;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(37, 184, 242, 0.22);
  border-radius: 24px;
  background:
    linear-gradient(rgba(18, 174, 231, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.14) 1px, transparent 1px),
    linear-gradient(135deg, rgba(255, 255, 255, 0.76), rgba(220, 247, 255, 0.6));
  background-size: 28px 28px, 28px 28px, auto;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86), 0 24px 46px rgba(18, 174, 231, 0.12);
}

.visual-screen::before {
  content: "";
  position: absolute;
  inset: 50% auto auto 50%;
  width: 54px;
  height: 54px;
  transform: translate(-50%, -50%);
  clip-path: polygon(30% 18%, 78% 50%, 30% 82%);
  background: linear-gradient(135deg, #25b8f2, #62d6bd);
  filter: drop-shadow(0 10px 22px rgba(18, 174, 231, 0.22));
}

.visual-screen span {
  position: absolute;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
}

.visual-screen span:nth-child(1) {
  left: 22px;
  right: 76px;
  bottom: 28px;
}

.visual-screen span:nth-child(2) {
  left: 22px;
  width: 58px;
  bottom: 48px;
}

.visual-screen span:nth-child(3) {
  right: 24px;
  width: 34px;
  bottom: 28px;
  background: rgba(37, 184, 242, 0.28);
}

.submission-layout {
  display: grid;
  grid-template-columns: minmax(280px, 0.72fr) minmax(0, 1.28fr);
  align-items: start;
  gap: 24px;
}

.submission-guide,
.submission-form {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  box-shadow: 0 24px 62px rgba(18, 85, 116, 0.1);
  backdrop-filter: blur(24px) saturate(1.08);
  -webkit-backdrop-filter: blur(24px) saturate(1.08);
}

.submission-guide {
  position: sticky;
  top: 92px;
  padding: 26px;
}

.guide-heading {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
}

.guide-icon {
  flex: 0 0 auto;
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  color: #087fc4;
  font-size: 22px;
  background: linear-gradient(135deg, #e2f8ff 0%, #c8efff 52%, #dcfff4 100%);
  border: 1px solid rgba(37, 184, 242, 0.28);
  border-radius: 16px;
}

.guide-heading h2,
.section-title h2 {
  margin: 0;
  color: #12384f;
  font-size: 20px;
  font-weight: 900;
  line-height: 1.25;
}

.guide-heading p {
  margin: 3px 0 0;
  color: #6a8294;
  font-size: 13px;
}

.guide-steps {
  display: grid;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.guide-steps li {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 13px;
}

.guide-steps li > span {
  height: 32px;
  display: grid;
  place-items: center;
  color: #0b6f9f;
  font-size: 12px;
  font-weight: 900;
  background: rgba(229, 249, 255, 0.78);
  border: 1px solid rgba(37, 184, 242, 0.18);
  border-radius: 999px;
}

.guide-steps strong {
  display: block;
  color: #12384f;
  font-size: 14px;
  font-weight: 850;
  line-height: 1.35;
}

.guide-steps p {
  margin: 4px 0 0;
  color: #5f788b;
  font-size: 13px;
  line-height: 1.6;
}

.privacy-note {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 24px;
  padding: 14px;
  color: #496579;
  font-size: 13px;
  line-height: 1.55;
  background: rgba(245, 251, 255, 0.78);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 16px;
}

.privacy-note .el-icon {
  margin-top: 2px;
  color: #087fc4;
}

.submission-form {
  padding: clamp(24px, 3vw, 34px);
}

.section-title {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.section-title span {
  display: block;
  margin-bottom: 4px;
  color: #087fc4;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.section-title em {
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

.submission-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.submission-form :deep(.el-form-item__label) {
  color: #31556e !important;
  font-size: 13px !important;
  font-weight: 850 !important;
  line-height: 1.2 !important;
  margin-bottom: 8px !important;
}

.submission-form :deep(.el-input__wrapper),
.submission-form :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.92) !important;
  border: 1px solid rgba(98, 177, 210, 0.22) !important;
  border-radius: 14px !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86) !important;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease !important;
}

.submission-form :deep(.el-input__wrapper) {
  min-height: 46px;
}

.submission-form :deep(.el-textarea__inner) {
  min-height: 126px !important;
  padding: 14px 16px !important;
  resize: vertical;
}

.submission-form :deep(.el-input__wrapper:hover),
.submission-form :deep(.el-textarea__inner:hover),
.submission-form :deep(.el-input__wrapper.is-focus),
.submission-form :deep(.el-textarea__inner:focus) {
  border-color: rgba(37, 184, 242, 0.52) !important;
  box-shadow: 0 0 0 4px rgba(37, 184, 242, 0.12) !important;
}

.submission-form :deep(.el-input__inner),
.submission-form :deep(.el-textarea__inner) {
  color: #12384f !important;
}

.submission-form :deep(.el-input__inner::placeholder),
.submission-form :deep(.el-textarea__inner::placeholder) {
  color: #7f9ab0 !important;
}

.submission-form :deep(.el-input__prefix-inner) {
  color: #5b7890 !important;
}

.code-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px;
  gap: 10px;
}

.code-row .el-button {
  min-width: 0;
  padding: 0 12px;
  color: #0b6f9f !important;
  font-weight: 850 !important;
  background: linear-gradient(135deg, #f7fdff 0%, #e0f5ff 52%, #ecfff9 100%) !important;
  border: 1px solid rgba(37, 184, 242, 0.32) !important;
  border-radius: 14px !important;
  box-shadow: 0 10px 22px rgba(18, 174, 231, 0.1) !important;
}

.code-row .el-button span {
  overflow: hidden;
  text-overflow: ellipsis;
}

.description-item,
.upload-item {
  margin-bottom: 18px !important;
}

.submission-form :deep(.el-upload),
.submission-form :deep(.el-upload-dragger) {
  width: 100%;
}

.submission-form :deep(.el-upload-dragger) {
  min-height: 184px;
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
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease !important;
}

.submission-form :deep(.el-upload-dragger:hover) {
  border-color: rgba(37, 184, 242, 0.72) !important;
  box-shadow: 0 16px 36px rgba(18, 174, 231, 0.12) !important;
}

.upload-icon {
  margin-bottom: 10px;
  color: #087fc4;
  font-size: 42px;
}

.submission-form :deep(.el-upload__text) {
  color: #4f6d82 !important;
  font-weight: 800;
}

.submission-form :deep(.el-upload__tip) {
  margin-top: 10px;
  color: #5f788b;
  font-size: 13px;
  line-height: 1.5;
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

.selected-file .el-icon {
  color: #087fc4;
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

.consent-row :deep(.el-checkbox) {
  height: auto;
  margin-top: 1px;
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
  color: #075985 !important;
  font-size: 16px;
  font-weight: 900;
  background: linear-gradient(135deg, #e2f8ff 0%, #c8efff 52%, #dcfff4 100%) !important;
  border: 1px solid rgba(37, 184, 242, 0.38) !important;
  border-radius: 14px !important;
  box-shadow: 0 14px 30px rgba(18, 174, 231, 0.14) !important;
}

.submit-button:hover,
.submit-button:focus-visible {
  color: #056f9a !important;
  border-color: rgba(37, 184, 242, 0.54) !important;
  box-shadow: 0 18px 38px rgba(18, 174, 231, 0.18) !important;
}

@media (max-width: 980px) {
  .submission-hero,
  .submission-layout {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    display: none;
  }

  .submission-guide {
    position: static;
  }
}

@media (max-width: 680px) {
  .submission-nav {
    grid-template-columns: 1fr auto;
  }

  .submission-nav strong {
    display: none;
  }

  .nav-link-right {
    justify-self: end;
  }

  .submission-main {
    width: min(100% - 24px, 560px);
    padding: 24px 0 46px;
  }

  .submission-hero,
  .submission-guide,
  .submission-form {
    border-radius: 24px;
  }

  .submission-hero {
    padding: 26px 20px;
  }

  .eyebrow {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .submission-hero h1 {
    font-size: 34px;
  }

  .hero-badges {
    gap: 8px;
  }

  .submission-guide,
  .submission-form {
    padding: 22px 18px;
  }

  .form-grid,
  .code-row {
    grid-template-columns: 1fr;
  }

  .section-title {
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

@media (max-width: 420px) {
  .submission-nav {
    padding-left: 12px;
    padding-right: 12px;
  }

  .nav-link {
    min-height: 38px;
    padding: 0 11px;
    font-size: 13px;
  }

  .submission-hero h1 {
    font-size: 30px;
  }
}
</style>
