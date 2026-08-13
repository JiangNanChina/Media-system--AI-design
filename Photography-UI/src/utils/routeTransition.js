import { readonly, ref } from 'vue'

const MINIMUM_DISPLAY_MS = 420

const routeTransitionActive = ref(true)
const routeTransitionTitle = ref('正在装载画面')
const routeTransitionCode = ref('00')

let navigationId = 0
let startedAt = Date.now()
let hideTimer = null
const navigationIds = new WeakMap()

const setBodyState = (active) => {
  if (typeof document === 'undefined') return
  document.body.classList.toggle('route-transition-active', active)
}

const startRouteTransition = (to) => {
  navigationId += 1
  if (to && typeof to === 'object') navigationIds.set(to, navigationId)
  startedAt = Date.now()
  routeTransitionTitle.value = to?.meta?.title || '正在切换画面'
  routeTransitionCode.value = String(navigationId % 100).padStart(2, '0')
  routeTransitionActive.value = true
  setBodyState(true)

  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
}

const finishRouteTransition = (expectedNavigationId = navigationId, minimumDisplayMs = MINIMUM_DISPLAY_MS) => {
  const remaining = Math.max(0, minimumDisplayMs - (Date.now() - startedAt))

  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    if (expectedNavigationId !== navigationId) return

    const hide = () => {
      if (expectedNavigationId !== navigationId) return
      routeTransitionActive.value = false
      setBodyState(false)
      hideTimer = null
    }

    if (typeof window === 'undefined') {
      hide()
      return
    }

    window.requestAnimationFrame(() => window.requestAnimationFrame(hide))
  }, remaining)
}

export const routeTransitionState = {
  active: readonly(routeTransitionActive),
  title: readonly(routeTransitionTitle),
  code: readonly(routeTransitionCode)
}

export const setupRouteTransitions = (router) => {
  const removeBefore = router.beforeEach((to) => {
    startRouteTransition(to)
    return true
  })

  const removeAfter = router.afterEach((to) => {
    finishRouteTransition(navigationIds.get(to) || navigationId)
  })

  const removeError = router.onError(() => {
    finishRouteTransition(navigationId, 0)
  })

  return () => {
    removeBefore?.()
    removeAfter?.()
    removeError?.()
    if (hideTimer) clearTimeout(hideTimer)
    setBodyState(false)
  }
}

export const finishInitialRouteTransition = () => {
  finishRouteTransition(navigationId)
}
