<template>
  <div v-if="enabled" aria-hidden="true">
    <div class="interactive-cursor" :style="cursorStyle"></div>
    <div class="interactive-cursor-dot" :style="dotStyle"></div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const enabled = ref(false)
const cursorX = ref(-100)
const cursorY = ref(-100)
const dotX = ref(-100)
const dotY = ref(-100)
const pressing = ref(false)

let frameId = 0
let targetX = -100
let targetY = -100

const cursorStyle = computed(() => ({
  transform: `translate3d(${cursorX.value}px, ${cursorY.value}px, 0) scale(${pressing.value ? 0.86 : 1})`
}))

const dotStyle = computed(() => ({
  transform: `translate3d(${dotX.value}px, ${dotY.value}px, 0)`
}))

const canUseCursor = () => {
  if (typeof window === 'undefined') return false
  const finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  return finePointer && !reducedMotion
}

const animate = () => {
  cursorX.value += (targetX - cursorX.value) * 0.22
  cursorY.value += (targetY - cursorY.value) * 0.22
  dotX.value = targetX
  dotY.value = targetY
  frameId = window.requestAnimationFrame(animate)
}

const setHoverState = (event) => {
  const target = event.target
  const hovering = Boolean(target?.closest?.('a, button, [role="button"], .el-button, .el-menu-item, .el-sub-menu__title, .card-hover, .quick-action, .equipment-card, .announcement-card, .device-card, .mode-tab, .avatar-overlay'))
  document.body.classList.toggle('cursor-hovering', hovering)
}

const handlePointerMove = (event) => {
  targetX = event.clientX
  targetY = event.clientY
  document.body.classList.add('cursor-ready')
  setHoverState(event)
}

const handlePointerDown = () => {
  pressing.value = true
  document.body.classList.add('cursor-pressing')
}

const handlePointerUp = () => {
  pressing.value = false
  document.body.classList.remove('cursor-pressing')
}

const cleanup = () => {
  window.cancelAnimationFrame(frameId)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerdown', handlePointerDown)
  window.removeEventListener('pointerup', handlePointerUp)
  document.body.classList.remove('cursor-ready', 'cursor-hovering', 'cursor-pressing', 'cursor-disabled')
}

onMounted(() => {
  enabled.value = canUseCursor()
  document.body.classList.toggle('cursor-disabled', !enabled.value)
  if (!enabled.value) return

  window.addEventListener('pointermove', handlePointerMove, { passive: true })
  window.addEventListener('pointerdown', handlePointerDown, { passive: true })
  window.addEventListener('pointerup', handlePointerUp, { passive: true })
  frameId = window.requestAnimationFrame(animate)
})

onBeforeUnmount(cleanup)
</script>
