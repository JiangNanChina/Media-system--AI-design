<template>
  <aside class="landing-live-preview">
    <div class="preview-heading">
      <div>
        <span>真实页面预览</span>
        <strong>{{ title }}</strong>
      </div>
      <a href="/" target="_blank" rel="noopener noreferrer" aria-label="在新窗口打开落地页">
        <el-icon><TopRight /></el-icon>
        打开落地页
      </a>
    </div>

    <el-segmented
      v-if="normalizedSections.length > 1"
      v-model="activeSection"
      class="preview-section-switcher"
      :options="normalizedSections"
      aria-label="切换预览区域"
    />

    <div class="preview-browser">
      <div class="preview-browser-bar" aria-hidden="true">
        <span></span>
        <span></span>
        <span></span>
        <div>{{ previewAddress }}</div>
      </div>
      <div ref="viewport" class="preview-viewport" :style="viewportStyle">
        <iframe
          ref="previewFrame"
          :src="previewUrl"
          :title="`${title}真实页面预览`"
          :style="frameStyle"
          tabindex="-1"
          @load="handleFrameLoad"
        ></iframe>
        <div v-if="!previewReady" class="preview-loading">正在载入真实页面...</div>
      </div>
    </div>

    <p class="preview-note">
      当前表单与已发布栏目会实时同步；预览使用和公开落地页完全相同的组件与样式。
    </p>
  </aside>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { TopRight } from '@element-plus/icons-vue'

const props = defineProps({
  settings: {
    type: Object,
    required: true
  },
  items: {
    type: Array,
    default: () => []
  },
  section: {
    type: String,
    default: 'hero'
  },
  sections: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: '公开落地页'
  }
})

const PREVIEW_WIDTH = 1080
const PREVIEW_HEIGHT = 1080
const previewFrame = ref(null)
const viewport = ref(null)
const previewReady = ref(false)
const previewScale = ref(0.4)
const activeSection = ref(props.section)
let resizeObserver = null

const normalizedSections = computed(() => props.sections.length
  ? props.sections
  : [{ label: props.title, value: props.section }])
const previewUrl = computed(() => `/?landingPreview=1#${encodeURIComponent(props.section)}`)
const previewAddress = computed(() => `公开落地页 / #${activeSection.value}`)
const viewportStyle = computed(() => ({ height: `${Math.round(PREVIEW_HEIGHT * previewScale.value)}px` }))
const frameStyle = computed(() => ({
  width: `${PREVIEW_WIDTH}px`,
  height: `${PREVIEW_HEIGHT}px`,
  transform: `scale(${previewScale.value})`
}))

const previewPayload = () => ({
  type: 'landing-preview-update',
  section: activeSection.value,
  content: {
    settings: { ...props.settings },
    campusFeatures: props.items
      .filter(item => item.sectionType === 'CAMPUS_FEATURE' && item.published !== false)
      .map(item => ({ ...item })),
    departmentShowcases: props.items
      .filter(item => item.sectionType === 'DEPARTMENT_SHOWCASE' && item.published !== false)
      .map(item => ({ ...item }))
  }
})

const postPreview = () => {
  const target = previewFrame.value?.contentWindow
  if (!target) return
  target.postMessage(previewPayload(), window.location.origin)
}

const handleFrameLoad = () => {
  previewReady.value = true
  postPreview()
}

const handlePreviewMessage = event => {
  if (event.origin !== window.location.origin || event.source !== previewFrame.value?.contentWindow) return
  if (event.data?.type !== 'landing-preview-ready') return
  previewReady.value = true
  postPreview()
}

const updateScale = () => {
  const width = viewport.value?.clientWidth || 0
  if (!width) return
  previewScale.value = Math.min(width / PREVIEW_WIDTH, 1)
}

watch(
  () => [props.settings, props.items],
  () => nextTick(postPreview),
  { deep: true }
)

watch(activeSection, () => nextTick(postPreview))

onMounted(() => {
  window.addEventListener('message', handlePreviewMessage)
  resizeObserver = new ResizeObserver(updateScale)
  if (viewport.value) resizeObserver.observe(viewport.value)
  updateScale()
})

onBeforeUnmount(() => {
  window.removeEventListener('message', handlePreviewMessage)
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.landing-live-preview {
  position: sticky;
  top: 20px;
  min-width: 0;
  display: grid;
  gap: 12px;
}

.preview-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #173f56;
}

.preview-heading > div {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.preview-heading span {
  color: #6a808b;
  font-size: 12px;
  font-weight: 700;
}

.preview-heading strong {
  overflow: hidden;
  font-size: 15px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-heading a {
  min-height: 40px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 0 0 auto;
  color: #167ca8;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  background: #f1f9fc;
  border: 1px solid rgba(86, 172, 204, 0.3);
  border-radius: 6px;
  transition: color 150ms ease, background-color 150ms ease, border-color 150ms ease, transform 150ms ease;
}

.preview-heading a:hover,
.preview-heading a:focus-visible {
  color: #075b80;
  background: #e4f5fa;
  border-color: rgba(47, 155, 195, 0.55);
  outline: none;
}

.preview-heading a:active {
  transform: scale(0.96);
}

.preview-browser {
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(188, 223, 235, 0.9);
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(31, 99, 132, 0.12);
}

.preview-section-switcher {
  justify-self: start;
  max-width: 100%;
}

.preview-browser-bar {
  height: 36px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 7px;
  background: #f1f8fb;
  border-bottom: 1px solid rgba(188, 223, 235, 0.72);
}

.preview-browser-bar > span {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  background: #9edbe9;
  border-radius: 50%;
}

.preview-browser-bar > span:nth-child(2) {
  background: #ffd58a;
}

.preview-browser-bar > span:nth-child(3) {
  background: #96dfb8;
}

.preview-browser-bar div {
  min-width: 0;
  height: 22px;
  margin-left: 5px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
  color: #718894;
  font-size: 10px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(188, 223, 235, 0.72);
  border-radius: 4px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-viewport {
  position: relative;
  width: 100%;
  min-height: 280px;
  overflow: hidden;
  background: #11100e;
  transition: height 180ms ease-out;
}

.preview-viewport iframe {
  position: absolute;
  top: 0;
  left: 0;
  display: block;
  background: #11100e;
  border: 0;
  transform-origin: top left;
  pointer-events: none;
}

.preview-loading {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: #6a808b;
  font-size: 13px;
  background: #f7fbfc;
}

.preview-note {
  margin: 0;
  color: #6a808b;
  font-size: 12px;
  line-height: 1.6;
  text-wrap: pretty;
}

@media (max-width: 1180px) {
  .landing-live-preview {
    position: static;
    max-width: 640px;
  }
}

@media (max-width: 520px) {
  .preview-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .preview-heading a {
    width: 100%;
  }

  .preview-viewport {
    min-height: 240px;
  }
}
</style>
