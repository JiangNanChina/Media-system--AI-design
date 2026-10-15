<template>
  <el-dialog
    v-model="dialogVisible"
    :class="['submission-receipt-dialog', `is-${variant}`]"
    modal-class="submission-receipt-overlay"
    width="min(620px, calc(100vw - 32px))"
    append-to-body
    align-center
    destroy-on-close
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <article class="receipt-shell" :aria-label="content.title">
      <span class="receipt-rail" aria-hidden="true"></span>

      <header class="receipt-header receipt-enter">
        <div class="receipt-heading">
          <span class="receipt-status-mark" aria-hidden="true">
            <el-icon><CircleCheckFilled /></el-icon>
          </span>
          <div>
            <small>{{ content.eyebrow }}</small>
            <h2>{{ content.title }}</h2>
          </div>
        </div>

        <div class="receipt-state" aria-label="状态：已接收">
          <span>STATUS</span>
          <strong><i></i>RECEIVED</strong>
        </div>

        <button
          type="button"
          class="receipt-close"
          aria-label="关闭并返回首页"
          title="关闭并返回首页"
          @click="confirmAndReturn"
        >
          <el-icon><Close /></el-icon>
        </button>
      </header>

      <div class="receipt-body receipt-enter">
        <p class="receipt-intro">{{ content.description }}</p>

        <section class="receipt-reference" :aria-label="content.referenceName">
          <div class="receipt-reference__header">
            <span>{{ content.referenceLabel }}</span>
            <button
              type="button"
              class="receipt-copy"
              :aria-label="`复制${content.referenceName}`"
              @click="copyReference"
            >
              <span class="receipt-copy__icons" aria-hidden="true">
                <el-icon class="receipt-copy__icon" :class="{ 'is-hidden': copied }"><CopyDocument /></el-icon>
                <el-icon class="receipt-copy__icon is-confirmed" :class="{ 'is-visible': copied }"><Check /></el-icon>
              </span>
              <span>{{ copied ? '已复制' : '复制编号' }}</span>
            </button>
          </div>
          <strong>{{ referenceNumber || '--' }}</strong>
        </section>

        <div class="receipt-review-note">
          <el-icon aria-hidden="true"><Lock /></el-icon>
          <span>
            <strong>PRIVATE REVIEW</strong>
            {{ content.followUp }}
          </span>
        </div>
      </div>

      <footer class="receipt-footer receipt-enter">
        <span class="receipt-serial"><i></i>{{ content.serialLabel }}</span>
        <el-button class="receipt-home-button" @click="confirmAndReturn">
          <span>返回首页</span>
          <el-icon><Right /></el-icon>
        </el-button>
      </footer>
    </article>
  </el-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { Check, CircleCheckFilled, Close, CopyDocument, Lock, Right } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  variant: {
    type: String,
    default: 'submission',
    validator: value => ['submission', 'join'].includes(value)
  },
  referenceNumber: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const variantContent = {
  submission: {
    eyebrow: 'SUBMISSION RECEIPT / RECEIVED',
    title: '投稿成功',
    description: '视频已完整送达融媒体中心，现已进入私有审核区。请保存本次投稿编号，便于后续核对。',
    referenceLabel: 'SUBMISSION NO.',
    referenceName: '投稿编号',
    followUp: '审核结果将通过你填写的 QQ 邮箱通知。',
    serialLabel: 'VIDEO DESK / FRAME 001'
  },
  join: {
    eyebrow: 'APPLICATION RECEIPT / RECEIVED',
    title: '申请已提交',
    description: '你的报名信息已送达融媒体中心，现已进入申请审核流程。请保存申请编号，便于后续核对。',
    referenceLabel: 'APPLICATION NO.',
    referenceName: '申请编号',
    followUp: '后续安排将通过你填写的 QQ 邮箱或手机号通知。',
    serialLabel: 'JOIN DESK / FRAME 001'
  }
}

const copied = ref(false)
let copiedTimer

const dialogVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const content = computed(() => variantContent[props.variant])

const fallbackCopy = value => {
  const textarea = document.createElement('textarea')
  textarea.value = value
  textarea.setAttribute('readonly', '')
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  textarea.remove()
}

const copyReference = async () => {
  if (!props.referenceNumber) return

  try {
    if (navigator.clipboard?.writeText) await navigator.clipboard.writeText(props.referenceNumber)
    else fallbackCopy(props.referenceNumber)
  } catch {
    fallbackCopy(props.referenceNumber)
  }

  copied.value = true
  clearTimeout(copiedTimer)
  copiedTimer = setTimeout(() => {
    copied.value = false
  }, 1800)
}

const confirmAndReturn = () => {
  dialogVisible.value = false
  emit('confirm')
}

watch(() => props.modelValue, visible => {
  if (!visible) copied.value = false
})

onBeforeUnmount(() => clearTimeout(copiedTimer))
</script>

<style scoped>
:global(.submission-receipt-overlay.el-overlay) {
  padding: 16px;
  background: rgba(17, 16, 14, 0.72) !important;
  backdrop-filter: blur(7px) saturate(0.72);
  -webkit-backdrop-filter: blur(7px) saturate(0.72);
}

:global(.submission-receipt-dialog.el-dialog) {
  --receipt-ink: #11100e;
  --receipt-paper: #f4f3ef;
  --receipt-white: #ffffff;
  --receipt-red: #d84a36;
  --receipt-yellow: #e4b84e;
  --receipt-grey: #a7adb0;
  --receipt-line: rgba(17, 16, 14, 0.22);
  --receipt-accent: var(--receipt-red);
  --receipt-accent-ink: var(--receipt-white);
  --receipt-offset: var(--receipt-yellow);
  --receipt-display: "Arial Black", "PingFang SC", "Microsoft YaHei", sans-serif;
  --receipt-body: "Noto Sans CJK SC", "Source Han Sans SC", "Microsoft YaHei", sans-serif;
  --receipt-mono: "Cascadia Mono", "SFMono-Regular", Consolas, monospace;
  max-width: 620px;
  padding: 0 !important;
  overflow: visible !important;
  color: var(--receipt-ink) !important;
  background: transparent !important;
  border: 0 !important;
  border-radius: 2px !important;
  box-shadow: none !important;
  font-family: var(--receipt-body);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

:global(.submission-receipt-dialog.is-join.el-dialog) {
  --receipt-accent: var(--receipt-yellow);
  --receipt-accent-ink: var(--receipt-ink);
  --receipt-offset: var(--receipt-red);
}

:global(.submission-receipt-dialog .el-dialog__header) {
  display: none !important;
}

:global(.submission-receipt-dialog .el-dialog__body) {
  padding: 0 !important;
  color: inherit !important;
}

.receipt-shell {
  position: relative;
  overflow: hidden;
  background-color: var(--receipt-white);
  background-image:
    linear-gradient(rgba(17, 16, 14, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 16, 14, 0.045) 1px, transparent 1px);
  background-size: 22px 22px;
  border: 1px solid rgba(255, 255, 255, 0.58);
  border-radius: 2px;
  box-shadow:
    9px 9px 0 var(--receipt-offset),
    0 0 0 1px rgba(17, 16, 14, 0.76),
    0 30px 80px rgba(0, 0, 0, 0.28);
}

.receipt-rail {
  position: absolute;
  z-index: 3;
  inset: 0 0 auto;
  height: 5px;
  background: var(--receipt-accent);
}

.receipt-header {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  min-height: 126px;
  padding: 26px 76px 24px 28px;
  color: var(--receipt-white);
  background: var(--receipt-ink);
  border-bottom: 1px solid rgba(255, 255, 255, 0.24);
}

.receipt-heading {
  display: flex;
  min-width: 0;
  gap: 16px;
  align-items: center;
}

.receipt-status-mark {
  display: grid;
  place-items: center;
  flex: 0 0 52px;
  width: 52px;
  height: 52px;
  color: var(--receipt-accent-ink);
  background: var(--receipt-accent);
  box-shadow: 5px 5px 0 rgba(255, 255, 255, 0.16);
}

.receipt-status-mark .el-icon {
  font-size: 28px;
}

.receipt-heading small,
.receipt-state,
.receipt-reference__header,
.receipt-serial,
.receipt-review-note strong {
  font-family: var(--receipt-mono);
  letter-spacing: 0;
}

.receipt-heading small {
  display: block;
  margin-bottom: 7px;
  color: var(--receipt-yellow);
  font-size: 9px;
  font-weight: 800;
  line-height: 1.4;
}

.receipt-heading h2 {
  margin: 0;
  color: var(--receipt-white);
  font-family: var(--receipt-display);
  font-size: 28px;
  line-height: 1.12;
  letter-spacing: 0;
  text-wrap: balance;
}

.receipt-state {
  display: grid;
  gap: 7px;
  justify-items: end;
  font-size: 8px;
  font-weight: 800;
}

.receipt-state span {
  color: rgba(255, 255, 255, 0.54);
}

.receipt-state strong {
  display: inline-flex;
  gap: 7px;
  align-items: center;
  color: var(--receipt-white);
  font-size: 10px;
}

.receipt-state i,
.receipt-serial i {
  display: inline-block;
  flex: 0 0 7px;
  width: 7px;
  height: 7px;
  background: var(--receipt-accent);
  border-radius: 50%;
}

.receipt-close {
  position: absolute;
  top: 18px;
  right: 18px;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  padding: 0;
  color: rgba(255, 255, 255, 0.72);
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 0;
  cursor: pointer;
  transition-property: color, background-color, border-color, scale;
  transition-duration: 150ms;
  transition-timing-function: ease-out;
}

.receipt-close:hover,
.receipt-close:focus-visible {
  color: var(--receipt-ink);
  background: var(--receipt-yellow);
  border-color: var(--receipt-yellow);
  outline: none;
}

.receipt-close:active {
  scale: 0.96;
}

.receipt-close .el-icon {
  font-size: 20px;
}

.receipt-body {
  display: grid;
  gap: 22px;
  padding: 26px 28px 28px;
}

.receipt-intro {
  max-width: 48ch;
  margin: 0;
  color: #4f504c;
  font-size: 14px;
  font-weight: 650;
  line-height: 1.8;
  text-wrap: pretty;
}

.receipt-reference {
  min-width: 0;
  padding: 18px 20px 20px;
  color: var(--receipt-white);
  background: var(--receipt-ink);
  border-left: 6px solid var(--receipt-accent);
  box-shadow: 6px 6px 0 rgba(167, 173, 176, 0.32);
}

.receipt-reference__header {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  margin-bottom: 14px;
  color: var(--receipt-yellow);
  font-size: 9px;
  font-weight: 800;
}

.receipt-reference > strong {
  display: block;
  min-width: 0;
  color: var(--receipt-white);
  font-family: var(--receipt-mono);
  font-size: 19px;
  font-variant-numeric: tabular-nums;
  font-weight: 800;
  line-height: 1.45;
  letter-spacing: 0;
  overflow-wrap: anywhere;
  user-select: all;
}

.receipt-copy {
  display: inline-flex;
  gap: 7px;
  align-items: center;
  min-height: 40px;
  padding: 0 12px;
  color: var(--receipt-ink);
  background: var(--receipt-yellow);
  border: 1px solid var(--receipt-yellow);
  border-radius: 0;
  font-family: var(--receipt-body);
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
  transition-property: color, background-color, border-color, scale;
  transition-duration: 150ms;
  transition-timing-function: ease-out;
}

.receipt-copy:hover,
.receipt-copy:focus-visible {
  color: var(--receipt-white);
  background: var(--receipt-red);
  border-color: var(--receipt-red);
  outline: none;
}

.receipt-copy:active {
  scale: 0.96;
}

.receipt-copy__icons {
  position: relative;
  width: 16px;
  height: 16px;
}

.receipt-copy__icon {
  position: absolute;
  inset: 0;
  font-size: 16px;
  opacity: 1;
  scale: 1;
  filter: blur(0);
  transition-property: opacity, scale, filter;
  transition-duration: 300ms;
  transition-timing-function: cubic-bezier(0.2, 0, 0, 1);
}

.receipt-copy__icon.is-confirmed,
.receipt-copy__icon.is-hidden {
  opacity: 0;
  scale: 0.25;
  filter: blur(4px);
}

.receipt-copy__icon.is-confirmed.is-visible {
  opacity: 1;
  scale: 1;
  filter: blur(0);
}

.receipt-review-note {
  display: flex;
  gap: 12px;
  align-items: center;
  min-width: 0;
  color: #5d5e58;
  font-size: 12px;
  font-weight: 650;
  line-height: 1.6;
  text-wrap: pretty;
}

.receipt-review-note > .el-icon {
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  color: var(--receipt-white);
  background: var(--receipt-red);
  font-size: 18px;
}

.receipt-review-note strong {
  display: block;
  margin-bottom: 2px;
  color: var(--receipt-ink);
  font-size: 8px;
}

.receipt-footer {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: space-between;
  min-height: 88px;
  padding: 18px 28px 22px;
  background: #eeece6;
  border-top: 1px solid var(--receipt-line);
}

.receipt-serial {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #696a64;
  font-size: 8px;
  font-weight: 800;
}

#app .receipt-home-button.el-button,
:global(.submission-receipt-dialog .receipt-home-button.el-button) {
  min-width: 150px;
  min-height: 48px;
  margin: 0;
  padding: 0 17px 0 19px;
  color: var(--receipt-accent-ink) !important;
  background: var(--receipt-accent) !important;
  border: 1px solid var(--receipt-accent) !important;
  border-radius: 0 !important;
  box-shadow: 5px 5px 0 var(--receipt-offset);
  font-family: var(--receipt-display);
  font-size: 14px;
  letter-spacing: 0;
  transition-property: color, background-color, border-color, box-shadow, transform, scale;
  transition-duration: 150ms;
  transition-timing-function: ease-out;
}

:global(.submission-receipt-dialog .receipt-home-button.el-button:hover),
:global(.submission-receipt-dialog .receipt-home-button.el-button:focus-visible) {
  color: var(--receipt-white) !important;
  background: var(--receipt-ink) !important;
  border-color: var(--receipt-ink) !important;
  box-shadow: 6px 6px 0 var(--receipt-offset);
  transform: translate(-1px, -1px);
  outline: none;
}

:global(.submission-receipt-dialog .receipt-home-button.el-button:active) {
  scale: 0.96;
}

:global(.submission-receipt-dialog .receipt-home-button .el-icon) {
  margin-left: 8px;
  font-size: 17px;
}

.receipt-enter {
  opacity: 0;
  transform: translateY(12px);
  filter: blur(4px);
  animation: receipt-block-enter 320ms ease-out forwards;
}

.receipt-body {
  animation-delay: 90ms;
}

.receipt-footer {
  animation-delay: 170ms;
}

@keyframes receipt-block-enter {
  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

:global(.dialog-fade-enter-active .submission-receipt-dialog),
:global(.dialog-fade-leave-active .submission-receipt-dialog) {
  transition-property: opacity, transform !important;
  transition-duration: 220ms !important;
  transition-timing-function: ease-out !important;
}

:global(.dialog-fade-enter-from .submission-receipt-dialog) {
  opacity: 0;
  transform: translateY(12px);
}

:global(.dialog-fade-leave-to .submission-receipt-dialog) {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 560px) {
  :global(.submission-receipt-overlay.el-overlay) {
    padding: 12px;
  }

  :global(.submission-receipt-dialog.el-dialog) {
    width: calc(100vw - 24px) !important;
  }

  .receipt-shell {
    box-shadow:
      6px 6px 0 var(--receipt-offset),
      0 0 0 1px rgba(17, 16, 14, 0.76),
      0 22px 54px rgba(0, 0, 0, 0.28);
  }

  .receipt-header {
    grid-template-columns: minmax(0, 1fr);
    min-height: 112px;
    padding: 22px 62px 20px 18px;
  }

  .receipt-heading {
    gap: 12px;
  }

  .receipt-status-mark {
    flex-basis: 44px;
    width: 44px;
    height: 44px;
    box-shadow: 4px 4px 0 rgba(255, 255, 255, 0.16);
  }

  .receipt-status-mark .el-icon {
    font-size: 24px;
  }

  .receipt-heading h2 {
    font-size: 23px;
  }

  .receipt-heading small {
    font-size: 8px;
  }

  .receipt-state {
    display: none;
  }

  .receipt-close {
    top: 16px;
    right: 14px;
  }

  .receipt-body {
    gap: 18px;
    padding: 22px 18px 24px;
  }

  .receipt-reference {
    padding: 15px 15px 17px;
    border-left-width: 5px;
    box-shadow: 4px 4px 0 rgba(167, 173, 176, 0.32);
  }

  .receipt-reference__header {
    gap: 10px;
    margin-bottom: 12px;
  }

  .receipt-reference > strong {
    font-size: 15px;
  }

  .receipt-copy {
    min-height: 44px;
    padding-inline: 10px;
  }

  .receipt-footer {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
    min-height: 0;
    padding: 18px 18px 22px;
  }

  .receipt-serial {
    align-self: flex-start;
  }

  :global(.submission-receipt-dialog .receipt-home-button.el-button) {
    width: 100%;
    min-width: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .receipt-enter {
    opacity: 1;
    transform: none;
    filter: none;
    animation: none;
  }

  .receipt-close,
  .receipt-copy,
  .receipt-copy__icon,
  :global(.submission-receipt-dialog .receipt-home-button.el-button),
  :global(.dialog-fade-enter-active .submission-receipt-dialog),
  :global(.dialog-fade-leave-active .submission-receipt-dialog) {
    transition-duration: 0.01ms !important;
  }
}
</style>
