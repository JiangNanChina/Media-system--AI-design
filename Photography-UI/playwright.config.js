import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: false,
  use: {
    baseURL: 'http://127.0.0.1:3000',
    channel: 'chrome',
    screenshot: 'only-on-failure',
    reducedMotion: 'reduce'
  },
  reporter: [['list'], ['html', { open: 'never' }]]
})

