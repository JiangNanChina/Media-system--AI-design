const publicMessageThemes = new Map([
  ['/', 'landing'],
  ['/join-us', 'join'],
  ['/submission', 'submission'],
  ['/login', 'auth'],
  ['/register', 'auth'],
  ['/forgot-password', 'recovery']
])

export const resolveMessageTheme = path => {
  const normalizedPath = path !== '/' ? path.replace(/\/+$/, '') : path
  return publicMessageThemes.get(normalizedPath) || 'admin'
}

export const applyMessageTheme = path => {
  const theme = resolveMessageTheme(path)
  if (typeof document !== 'undefined') document.body.dataset.messageTheme = theme
  return theme
}

export const setupMessageTheme = router => {
  if (typeof window !== 'undefined') applyMessageTheme(window.location.pathname)
  router.afterEach(to => applyMessageTheme(to.path))
}
