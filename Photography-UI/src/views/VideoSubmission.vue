<template>
  <div class="submission-page">
    <header class="submission-nav">
      <router-link to="/" class="nav-link nav-link-back">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </router-link>

      <router-link to="/" class="submission-brand" aria-label="返回融媒体中心首页">
        <span class="submission-brand-mark"><el-icon><VideoCamera /></el-icon></span>
        <span>
          <strong>融媒体中心</strong>
          <small>Submission desk</small>
        </span>
      </router-link>

      <router-link to="/login" class="nav-link nav-link-right">
        <el-icon><Monitor /></el-icon>
        <span>管理平台</span>
      </router-link>
    </header>

    <main class="submission-main">
      <section class="submission-hero">
        <div class="hero-copy">
          <span class="eyebrow"><i></i>VIDEO SUBMISSION / OPEN CALL</span>
          <h1>让校园故事被更多人看见</h1>
          <p>上传视频、留下联系方式，融媒体中心会在私有审核区处理稿件并与你确认后续发布安排。</p>
          <div class="hero-badges" aria-label="投稿要求">
            <span><small>FORMAT</small><strong>MP4 / MOV / WebM</strong></span>
            <span><small>LIMIT</small><strong>最大 500MB</strong></span>
            <span><small>VERIFY</small><strong>邮箱验证后提交</strong></span>
          </div>
        </div>

        <div class="hero-visual" aria-hidden="true">
          <div class="visual-screen">
            <div class="screen-meta"><span>REC</span><span>16:9</span></div>
            <div class="screen-focus"></div>
            <div class="screen-play"></div>
            <div class="screen-timecode">00:00:00:00</div>
          </div>
        </div>
        <div class="hero-frame-label" aria-hidden="true">MASTER / 001</div>
      </section>

      <section class="submission-layout" aria-label="视频投稿表单">
        <aside class="submission-guide">
          <div class="guide-heading">
            <div>
              <span>Submission route</span>
              <h2>投稿流程</h2>
              <p>完成工作单后进入私有审核区</p>
            </div>
            <span class="guide-icon"><el-icon><DocumentChecked /></el-icon></span>
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
            <span><strong>PRIVATE REVIEW</strong>投稿资料仅用于稿件审核与沟通。</span>
          </div>
        </aside>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="submission-form"
        >
          <div class="section-title">
            <div>
              <span>Submission file / Master 001</span>
              <h2>投稿审核工作单</h2>
            </div>
            <div class="form-status">
              <span>STATUS</span>
              <strong><i></i>待填写</strong>
            </div>
          </div>

          <section class="form-section" aria-labelledby="submission-information-title">
            <header class="form-section-header">
              <span>A</span>
              <div>
                <small>Story & contact</small>
                <h3 id="submission-information-title">稿件与联系人</h3>
              </div>
              <em>带 * 为必填项</em>
            </header>

            <div class="form-grid">
              <el-form-item label="投稿标题" prop="title">
                <el-input
                  v-model.trim="form.title"
                  maxlength="160"
                  show-word-limit
                  size="large"
                  placeholder="请输入视频标题"
                >
                  <template #prefix><el-icon><EditPen /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="投稿人姓名" prop="submitterName">
                <el-input
                  v-model.trim="form.submitterName"
                  maxlength="80"
                  size="large"
                  placeholder="请输入真实姓名"
                >
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model.trim="form.phone"
                  maxlength="11"
                  size="large"
                  placeholder="请输入手机号"
                >
                  <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="学院或部门">
                <el-input
                  v-model.trim="form.organization"
                  maxlength="160"
                  placeholder="选填"
                  size="large"
                >
                  <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="QQ邮箱" prop="email">
                <el-input
                  v-model.trim="form.email"
                  type="email"
                  size="large"
                  placeholder="请输入QQ邮箱"
                >
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="邮箱验证码" prop="code">
                <div class="code-row">
                  <el-input
                    v-model.trim="form.code"
                    maxlength="6"
                    size="large"
                    placeholder="请输入6位验证码"
                  >
                    <template #prefix><el-icon><Message /></el-icon></template>
                  </el-input>
                  <el-button
                    size="large"
                    :disabled="countdown > 0"
                    :loading="sending"
                    @click="sendCode"
                  >
                    <el-icon><Promotion /></el-icon>
                    <span>{{ countdown ? `${countdown}s` : '发送验证码' }}</span>
                  </el-button>
                </div>
              </el-form-item>
            </div>
          </section>

          <section class="form-section" aria-labelledby="description-title">
            <header class="form-section-header">
              <span>B</span>
              <div>
                <small>Editorial notes</small>
                <h3 id="description-title">内容说明</h3>
              </div>
              <em>选填</em>
            </header>

            <el-form-item label="投稿说明" class="description-item content-form-item">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                maxlength="5000"
                show-word-limit
                placeholder="可补充拍摄背景、人物信息、活动时间地点或希望呈现的重点"
              />
            </el-form-item>
          </section>

          <section class="form-section master-section" aria-labelledby="video-master-title">
            <header class="form-section-header">
              <span>C</span>
              <div>
                <small>Video master</small>
                <h3 id="video-master-title">视频母带</h3>
              </div>
              <em>最大 500MB</em>
            </header>

            <el-form-item
              label="视频文件"
              prop="file"
              class="upload-item content-form-item"
            >
              <el-upload
                ref="uploadRef"
                drag
                :auto-upload="false"
                :limit="1"
                :show-file-list="false"
                :disabled="uploading"
                :class="{ 'has-file': fileMeta }"
                accept=".mp4,.mov,.webm,video/mp4,video/quicktime,video/webm"
                :on-change="selectFile"
                :on-remove="removeFile"
                :on-exceed="replaceFile"
              >
                <div v-if="!fileMeta" class="upload-empty">
                  <div class="upload-frame" aria-hidden="true">
                    <el-icon><VideoCamera /></el-icon>
                  </div>
                  <div class="upload-empty__copy">
                    <small>INGEST / 等待素材</small>
                    <strong>拖入视频母带</strong>
                    <span>或点击从设备选择文件</span>
                  </div>
                  <span class="upload-browse" aria-hidden="true">
                    <el-icon><UploadFilled /></el-icon>
                    选择视频
                  </span>
                </div>

                <div v-else class="selected-file">
                  <div class="selected-file__mark" aria-hidden="true">
                    <el-icon><VideoCamera /></el-icon>
                    <span>01</span>
                  </div>
                  <div class="selected-file__body">
                    <small>MASTER READY · {{ fileMeta.extension }}</small>
                    <strong :title="fileMeta.name">{{ fileMeta.name }}</strong>
                    <div class="selected-file__meta">
                      <span>{{ fileMeta.size }}</span>
                      <span>{{ uploadFailed ? '传输中断' : uploading ? '正在传输' : '等待提交' }}</span>
                    </div>
                  </div>
                  <div v-if="!uploading" class="selected-file__actions">
                    <el-button class="replace-file-button" @click.stop="openFilePicker">
                      <el-icon><RefreshRight /></el-icon>
                      <span>替换</span>
                    </el-button>
                    <el-tooltip content="移除视频" placement="top">
                      <el-button
                        class="remove-file-button"
                        aria-label="移除视频"
                        @click.stop="removeSelectedFile"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                  <FileUploadProgress
                    v-if="uploading || uploadFailed"
                    :percentage="progress"
                    :status="uploadProgressStatus"
                    subject="视频"
                    progress-color="#d84a36"
                  />
                </div>
              </el-upload>

              <div class="upload-specs" aria-label="视频上传要求">
                <span><strong>FORMAT</strong>MP4 · MOV · WebM</span>
                <span><strong>CAPACITY</strong>单文件最大 500MB</span>
                <span><el-icon><Lock /></el-icon>文件会进入私有审核区</span>
              </div>
            </el-form-item>
          </section>

          <div class="form-submit-area">
            <label class="consent-row">
              <el-checkbox v-model="consent" />
              <span>我确认拥有投稿内容的使用授权，并同意融媒体中心联系我处理稿件。</span>
            </label>

            <el-button
              class="submit-button"
              type="primary"
              size="large"
              :loading="uploading"
              :disabled="!consent"
              @click="submit"
            >
              <span>提交视频</span>
              <el-icon><UploadFilled /></el-icon>
            </el-button>
          </div>
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
  Delete,
  DocumentChecked,
  EditPen,
  Lock,
  Message,
  Monitor,
  OfficeBuilding,
  Phone,
  Promotion,
  RefreshRight,
  UploadFilled,
  User,
  VideoCamera
} from '@element-plus/icons-vue'
import { genFileId } from 'element-plus'
import request from '@/utils/request'
import FileUploadProgress from '@/components/FileUploadProgress.vue'
import { getUploadPercentage } from '@/utils/uploadProgress'

const router = useRouter()
const formRef = ref()
const uploadRef = ref()
const file = ref(null)
const consent = ref(false)
const sending = ref(false)
const uploading = ref(false)
const uploadFailed = ref(false)
const progress = ref(0)
const countdown = ref(0)
let timer

const form = reactive({
  title: '',
  submitterName: '',
  phone: '',
  organization: '',
  email: '',
  code: '',
  description: '',
  file: null
})

const rules = {
  title: [{ required: true, message: '请输入投稿标题', trigger: 'blur' }],
  submitterName: [{ required: true, message: '请输入投稿人姓名', trigger: 'blur' }],
  phone: [
    {
      required: true,
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效手机号',
      trigger: 'blur'
    }
  ],
  email: [{ required: true, type: 'email', message: '请输入有效QQ邮箱', trigger: 'blur' }],
  code: [
    {
      required: true,
      pattern: /^\d{6}$/,
      message: '请输入6位验证码',
      trigger: 'blur'
    }
  ],
  file: [
    {
      validator: (_rule, _value, callback) => {
        file.value ? callback() : callback(new Error('请选择投稿视频'))
      }
    }
  ]
}

const formatFileSize = bytes => {
  if (!bytes) return '0 MB'
  const mb = bytes / 1024 / 1024
  return `${mb >= 10 ? Math.round(mb) : mb.toFixed(1)} MB`
}

const fileMeta = computed(() => file.value
  ? {
      name: file.value.name,
      size: formatFileSize(file.value.size),
      extension: file.value.name.split('.').pop()?.toUpperCase() || 'VIDEO'
    }
  : null)

const uploadProgressStatus = computed(() => {
  if (uploadFailed.value) return 'error'
  return progress.value >= 100 ? 'processing' : 'uploading'
})

const selectFile = uploadFile => {
  uploadFailed.value = false
  progress.value = 0
  if (uploadFile.raw.size > 500 * 1024 * 1024) {
    ElMessage.error('视频不能超过500MB')
    removeSelectedFile()
    return
  }
  if (!/\.(mp4|mov|webm)$/i.test(uploadFile.name)) {
    ElMessage.error('仅支持MP4、MOV和WebM')
    removeSelectedFile()
    return
  }
  file.value = uploadFile.raw
  form.file = uploadFile.raw
  formRef.value?.validateField('file')
}

const removeFile = () => {
  file.value = null
  form.file = null
  uploadFailed.value = false
  progress.value = 0
}

const removeSelectedFile = () => {
  uploadRef.value?.clearFiles()
  removeFile()
}

const openFilePicker = () => {
  uploadRef.value?.$el?.querySelector('input[type="file"]')?.click()
}

const replaceFile = files => {
  const nextFile = files[0]
  if (!nextFile) return
  uploadRef.value?.clearFiles()
  nextFile.uid = genFileId()
  uploadRef.value?.handleStart(nextFile)
}

const sendCode = async () => {
  await formRef.value?.validateField('email')
  sending.value = true
  try {
    await request.post('/submissions/public/email-code', { email: form.email })
    countdown.value = 60
    timer = setInterval(() => {
      if (--countdown.value <= 0) clearInterval(timer)
    }, 1000)
    ElMessage.success('验证码已发送')
  } finally {
    sending.value = false
  }
}

const submit = async () => {
  await formRef.value?.validate()
  if (!consent.value) return ElMessage.warning('请先确认投稿授权')

  const data = new FormData()
  Object.entries(form)
    .filter(([key]) => key !== 'file')
    .forEach(([key, value]) => data.append(key, value || ''))
  data.append('file', file.value)

  uploading.value = true
  uploadFailed.value = false
  progress.value = 0
  let response
  try {
    response = await request.post('/submissions/public', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 10 * 60 * 1000,
      onUploadProgress: event => {
        const percentage = getUploadPercentage(event)
        if (percentage !== null) progress.value = percentage
      }
    })
    progress.value = 100
  } catch {
    uploadFailed.value = true
    return
  } finally {
    uploading.value = false
  }

  await ElMessageBox.alert(
    `投稿编号：${response.data.submissionNumber}`,
    '投稿成功',
    { confirmButtonText: '返回首页' }
  )
  router.push('/')
}

onMounted(async () => {
  const status = await request.get('/maintenance/public/status', { silent: true })
  if (status.data?.enabled && !status.data?.unlocked) {
    router.replace({ path: '/maintenance', query: { redirect: '/submission' } })
  }
})

onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped src="../styles/video-submission.css"></style>
