<template>
  <div class="join-page">
    <header class="join-nav">
      <router-link to="/" class="nav-link nav-link-back">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </router-link>

      <router-link to="/" class="join-brand" aria-label="返回融媒体中心首页">
        <span class="join-brand-mark">
          <img :src="logoUrl" alt="" />
        </span>
        <span>
          <strong>融媒体中心</strong>
          <small>Campus media center</small>
        </span>
      </router-link>

      <router-link to="/login" class="nav-link nav-link-right">
        <el-icon><Monitor /></el-icon>
        <span>管理平台</span>
      </router-link>
    </header>

    <main class="join-main">
      <section class="join-layout" aria-label="入部申请表单">
        <aside class="join-panel">
          <div class="panel-visual">
            <div class="visual-status">
              <span><i></i>OPEN CALL</span>
              <span>FILE 001</span>
            </div>
            <div class="visual-rings" aria-hidden="true"></div>
            <div class="visual-logo">
              <img :src="logoUrl" alt="融媒体中心标志" />
            </div>
            <div class="visual-caption">
              <span>RECRUITMENT</span>
              <strong>{{ currentYear }}</strong>
            </div>
          </div>

          <div class="panel-copy">
            <span>Join the team / 加入我们</span>
            <h1>提交入部申请</h1>
            <p>填写基本信息和自我介绍，作品可选上传。申请通过后，系统会通过 QQ 邮箱发送面试群通知。</p>
          </div>

          <div class="panel-steps">
            <div>
              <strong>01</strong>
              <span><b>填写资料</b><small>完成申请工作单</small></span>
            </div>
            <div>
              <strong>02</strong>
              <span><b>等待审核</b><small>关注审核结果</small></span>
            </div>
            <div>
              <strong>03</strong>
              <span><b>进入面试</b><small>查收 QQ 邮件通知</small></span>
            </div>
          </div>
        </aside>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="join-form">
          <div class="form-title">
            <div>
              <span>Application file / {{ currentYear }}</span>
              <h2>入部申请工作单</h2>
            </div>
            <div class="form-meta">
              <span>STATUS</span>
              <strong><i></i>待填写</strong>
            </div>
          </div>

          <section class="form-section" aria-labelledby="basic-information-title">
            <header class="form-section-header">
              <span>A</span>
              <div>
                <small>Basic information</small>
                <h3 id="basic-information-title">基本资料</h3>
              </div>
              <em>带 * 为必填项</em>
            </header>

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
          </section>

          <section class="form-section" aria-labelledby="introduction-title">
            <header class="form-section-header">
              <span>B</span>
              <div>
                <small>About you</small>
                <h3 id="introduction-title">自我介绍</h3>
              </div>
            </header>

            <el-form-item label="自我介绍" prop="selfIntroduction" class="wide-item content-form-item">
              <el-input
                v-model="form.selfIntroduction"
                type="textarea"
                :rows="6"
                maxlength="5000"
                show-word-limit
                placeholder="可以介绍你的兴趣方向、过往经历、想加入融媒体中心的原因"
              />
            </el-form-item>
          </section>

          <section class="form-section" aria-labelledby="portfolio-title">
            <header class="form-section-header">
              <span>C</span>
              <div>
                <small>Portfolio</small>
                <h3 id="portfolio-title">自我作品</h3>
              </div>
              <em>选填</em>
            </header>

            <el-form-item label="自我作品（选填）" class="wide-item content-form-item">
              <el-upload
                drag
                :auto-upload="false"
                :limit="1"
                :disabled="submitting"
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
                <FileUploadProgress
                  v-if="submitting || uploadFailed"
                  :percentage="progress"
                  :status="uploadProgressStatus"
                  subject="作品"
                  progress-color="#e4b84e"
                />
              </div>
            </el-form-item>
          </section>

          <div class="form-submit-area">
            <label class="consent-row">
              <el-checkbox v-model="consent" />
              <span>我确认以上信息真实有效，并同意融媒体中心通过 QQ 邮箱或手机号联系我。</span>
            </label>

            <el-button class="submit-button" type="primary" size="large" :loading="submitting" :disabled="!consent" @click="submit">
              <span>提交申请</span>
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </el-form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
import FileUploadProgress from '@/components/FileUploadProgress.vue'
import { getUploadPercentage } from '@/utils/uploadProgress'

const router = useRouter()
const formRef = ref()
const consent = ref(false)
const work = ref(null)
const colleges = ref([])
const submitting = ref(false)
const uploadFailed = ref(false)
const progress = ref(0)
const siteLogo = ref('')
const logoUrl = computed(() => siteLogo.value || '/logo.svg')

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

const uploadProgressStatus = computed(() => {
  if (uploadFailed.value) return 'error'
  return progress.value >= 100 ? 'processing' : 'uploading'
})

const selectWork = uploadFile => {
  uploadFailed.value = false
  progress.value = 0
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
  uploadFailed.value = false
  progress.value = 0
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
  uploadFailed.value = false
  progress.value = 0
  let response
  try {
    response = await request.post('/join-applications/public', data, {
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
    submitting.value = false
  }

  await ElMessageBox.alert(`申请编号：${response.data.applicationNumber}`, '申请已提交', {
    confirmButtonText: '返回首页'
  })
  router.push('/')
}

onMounted(async () => {
  fetchColleges()
  fetchSiteConfig()
  const status = await request.get('/maintenance/public/status', { silent: true })
  if (status.data?.enabled && !status.data?.unlocked) router.replace({ path: '/maintenance', query: { redirect: '/join-us' } })
})

</script>

<style scoped src="../styles/join-us.css"></style>
