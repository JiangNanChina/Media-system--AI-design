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
                  popper-class="join-select-popper"
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
                <el-select
                  v-model="form.enrollmentYear"
                  size="large"
                  placeholder="请选择入学年份"
                  popper-class="join-select-popper"
                >
                  <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
                </el-select>
              </el-form-item>

              <el-form-item label="班级" prop="className">
                <el-input v-model.trim="form.className" maxlength="80" size="large" placeholder="请输入班级" />
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
              <div
                class="upload-dropzone"
                :class="{ 'has-files': selectedEntries.length, 'is-disabled': submitting }"
                @dragover.prevent
                @drop.prevent="handleDrop"
              >
                <input
                  ref="fileInputRef"
                  class="upload-native-input"
                  type="file"
                  multiple
                  accept=".jpg,.jpeg,.png,.gif,.bmp,.webp,.mp4,.mov,.webm,image/jpeg,image/png,image/gif,image/bmp,image/webp,video/mp4,video/quicktime,video/webm"
                  @change="handleFileInput"
                />
                <input
                  ref="folderInputRef"
                  class="upload-native-input"
                  type="file"
                  multiple
                  webkitdirectory
                  directory
                  @change="handleFolderInput"
                />

                <div v-if="!selectedEntries.length" class="upload-empty">
                  <div class="upload-frame" aria-hidden="true">
                    <el-icon><Files /></el-icon>
                  </div>
                  <div class="upload-empty__copy">
                    <small>PORTFOLIO / 等待作品</small>
                    <strong>拖入一个或多个作品</strong>
                    <span>多个作品或文件夹会自动整理为 ZIP</span>
                  </div>
                  <div class="upload-picker-actions">
                    <el-button class="upload-browse" @click="openFilePicker">
                      <el-icon><UploadFilled /></el-icon>
                      <span>选择文件</span>
                    </el-button>
                    <el-button class="upload-folder-button" @click="openFolderPicker">
                      <el-icon><FolderOpened /></el-icon>
                      <span>选择文件夹</span>
                    </el-button>
                  </div>
                </div>

                <div v-else class="selected-file">
                  <div class="selected-file__mark" aria-hidden="true">
                    <el-icon>
                      <Files v-if="selection.requiresArchive" />
                      <VideoCamera v-else-if="selectedEntries[0].isVideo" />
                      <Picture v-else />
                    </el-icon>
                    <span>{{ fileCountLabel }}</span>
                  </div>
                  <div class="selected-file__body">
                    <small>{{ selection.requiresArchive ? 'PORTFOLIO READY · ZIP' : `WORK READY · ${selectedEntries[0].extension.toUpperCase()}` }}</small>
                    <strong :title="selectionTitle">{{ selectionTitle }}</strong>
                    <div class="selected-file__meta">
                      <span>{{ selectedEntries.length }} 个文件 · {{ formatUploadSize(selection.totalBytes) }}</span>
                      <span>{{ selectedFileStatus }}</span>
                    </div>
                  </div>
                  <div v-if="!submitting" class="selected-file__actions">
                    <el-button class="replace-file-button" @click="openFilePicker">
                      <el-icon><Plus /></el-icon>
                      <span>添加文件</span>
                    </el-button>
                    <el-button class="folder-file-button" @click="openFolderPicker">
                      <el-icon><FolderOpened /></el-icon>
                      <span>添加文件夹</span>
                    </el-button>
                    <el-tooltip content="清空全部文件" placement="top">
                      <el-button
                        class="remove-file-button"
                        aria-label="清空全部文件"
                        @click="clearSelectedWorks"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>

                  <ul class="file-manifest" aria-label="已选择的作品文件">
                    <li v-for="(entry, index) in selectedEntries" :key="entry.id">
                      <span class="file-manifest__index">{{ String(index + 1).padStart(2, '0') }}</span>
                      <span class="file-manifest__info">
                        <strong :title="entry.path">{{ entry.path }}</strong>
                        <small>{{ entry.extension.toUpperCase() }} · {{ formatUploadSize(entry.file.size) }}</small>
                      </span>
                      <el-tooltip content="移除此文件" placement="top">
                        <el-button
                          class="file-manifest__remove"
                          :aria-label="`移除 ${entry.path}`"
                          :disabled="submitting"
                          @click="removeEntry(index)"
                        >
                          <el-icon><Close /></el-icon>
                        </el-button>
                      </el-tooltip>
                    </li>
                  </ul>
                  <FileUploadProgress
                    v-if="submitting || uploadFailed"
                    :percentage="packing ? packingProgress : progress"
                    :status="uploadProgressStatus"
                    :subject="selection.requiresArchive ? '作品包' : '作品'"
                    progress-color="#d84a36"
                  />
                </div>
              </div>

              <div class="upload-specs" aria-label="作品上传要求">
                <span><strong>FORMAT</strong>JPG · PNG · WebP · MP4 · MOV · WebM</span>
                <span><strong>CAPACITY</strong>合计最大 300MB · 最多 500 个</span>
                <span>
                  <el-icon><Lock /></el-icon>
                  作品仅用于申请审核
                </span>
              </div>
            </el-form-item>
          </section>

          <div class="form-submit-area">
            <label class="consent-row">
              <el-checkbox v-model="consent" />
              <span>我确认以上信息真实有效，并同意融媒体中心通过 QQ 邮箱或手机号联系我。</span>
            </label>

            <el-button class="submit-button" type="primary" size="large" :loading="submitting" :disabled="!consent || submitting" @click="submit">
              <span>{{ packing ? '正在打包' : submitting ? '正在提交' : '提交申请' }}</span>
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </el-form>
      </section>
    </main>

    <SubmissionSuccessDialog
      v-model="successDialogVisible"
      variant="join"
      :reference-number="applicationNumber"
      @confirm="returnHome"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Close,
  Delete,
  Files,
  FolderOpened,
  Lock,
  Message,
  Monitor,
  Phone,
  Picture,
  Plus,
  Promotion,
  Reading,
  School,
  UploadFilled,
  User,
  VideoCamera
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'
import FileUploadProgress from '@/components/FileUploadProgress.vue'
import SubmissionSuccessDialog from '@/components/SubmissionSuccessDialog.vue'
import { getUploadPercentage } from '@/utils/uploadProgress'
import {
  createUploadSelection,
  formatUploadSize,
  prepareUploadFile
} from '@/utils/uploadArchive'

const router = useRouter()
const formRef = ref()
const fileInputRef = ref()
const folderInputRef = ref()
const consent = ref(false)
const selection = ref({ entries: [], totalBytes: 0, requiresArchive: false })
const colleges = ref([])
const submitting = ref(false)
const packing = ref(false)
const uploadFailed = ref(false)
const progress = ref(0)
const packingProgress = ref(0)
const siteLogo = ref('')
const successDialogVisible = ref(false)
const applicationNumber = ref('')
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
  className: '',
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
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  selfIntroduction: [{ required: true, message: '请输入自我介绍', trigger: 'blur' }]
}

const workSelectionOptions = {
  allowedExtensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'mp4', 'mov', 'webm'],
  maxBytes: 300 * 1024 * 1024,
  emptyFileMessage: '不能上传空作品文件',
  unsupportedMessage: '仅支持 JPG、PNG、GIF、WebP、BMP、MP4、MOV 和 WebM',
  sizeMessage: '所选作品总大小不能超过300MB'
}

const selectedEntries = computed(() => selection.value.entries.map(entry => ({
  ...entry,
  isVideo: ['mp4', 'mov', 'webm'].includes(entry.extension)
})))
const fileCountLabel = computed(() => String(selectedEntries.value.length).padStart(2, '0'))
const selectionTitle = computed(() => selection.value.requiresArchive
  ? `${selectedEntries.value.length} 个作品将合并为 ZIP 作品包`
  : selectedEntries.value[0]?.path || '')
const selectedFileStatus = computed(() => {
  if (uploadFailed.value) return '传输中断'
  if (packing.value) return '正在打包'
  if (submitting.value) return '正在传输'
  return selection.value.requiresArchive ? '提交时自动打包' : '等待提交'
})

const uploadProgressStatus = computed(() => {
  if (packing.value) return 'packing'
  if (uploadFailed.value) return 'error'
  return progress.value >= 100 ? 'processing' : 'uploading'
})

const applyFiles = files => {
  if (submitting.value || !files?.length) return
  uploadFailed.value = false
  progress.value = 0
  packingProgress.value = 0

  try {
    const combinedFiles = [
      ...selection.value.entries.map(entry => entry.file),
      ...Array.from(files)
    ]
    selection.value = createUploadSelection(combinedFiles, workSelectionOptions)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const resetNativeInput = event => {
  if (event?.target) event.target.value = ''
}

const handleFileInput = event => {
  applyFiles(event.target.files)
  resetNativeInput(event)
}

const handleFolderInput = event => {
  applyFiles(event.target.files)
  resetNativeInput(event)
}

const handleDrop = event => {
  if (submitting.value) return
  const containsFolder = Array.from(event.dataTransfer?.items || [])
    .some(item => item.webkitGetAsEntry?.()?.isDirectory)
  if (containsFolder) {
    ElMessage.warning('拖入文件夹时浏览器无法稳定读取目录，请使用“选择文件夹”')
    return
  }
  applyFiles(event.dataTransfer?.files)
}

const clearSelectedWorks = () => {
  selection.value = { entries: [], totalBytes: 0, requiresArchive: false }
  uploadFailed.value = false
  progress.value = 0
  packingProgress.value = 0
}

const removeEntry = index => {
  const remainingFiles = selection.value.entries
    .filter((_entry, entryIndex) => entryIndex !== index)
    .map(entry => entry.file)
  selection.value = createUploadSelection(remainingFiles, workSelectionOptions)
  uploadFailed.value = false
  progress.value = 0
  packingProgress.value = 0
}

const openFilePicker = () => {
  if (!submitting.value) fileInputRef.value?.click()
}

const openFolderPicker = () => {
  if (!submitting.value) folderInputRef.value?.click()
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

  submitting.value = true
  packing.value = selection.value.requiresArchive
  uploadFailed.value = false
  progress.value = 0
  packingProgress.value = 0
  let response
  let requestStarted = false
  try {
    const uploadFile = await prepareUploadFile(selection.value, {
      archiveBaseName: 'join-portfolio',
      maxBytes: workSelectionOptions.maxBytes,
      onProgress: percentage => { packingProgress.value = percentage }
    })
    packing.value = false

    const data = new FormData()
    Object.entries(form).forEach(([key, value]) => data.append(key, value ?? ''))
    if (uploadFile) data.append('work', uploadFile)

    requestStarted = true
    response = await request.post('/join-applications/public', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 10 * 60 * 1000,
      onUploadProgress: event => {
        const percentage = getUploadPercentage(event)
        if (percentage !== null) progress.value = percentage
      }
    })
    progress.value = 100
  } catch (error) {
    if (requestStarted) uploadFailed.value = true
    else ElMessage.error(error.message || '作品打包失败，请重试')
    return
  } finally {
    packing.value = false
    submitting.value = false
  }

  applicationNumber.value = response.data.applicationNumber
  successDialogVisible.value = true
}

const returnHome = () => router.push('/')

onMounted(async () => {
  fetchColleges()
  fetchSiteConfig()
  const status = await request.get('/maintenance/public/status', { silent: true })
  if (status.data?.enabled && !status.data?.unlocked) router.replace({ path: '/maintenance', query: { redirect: '/join-us' } })
})

</script>

<style scoped src="../styles/join-us.css"></style>
