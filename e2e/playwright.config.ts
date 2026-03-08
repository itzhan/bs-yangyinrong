import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  retries: 1,
  reporter: [['html', { open: 'never' }], ['list']],
  use: {
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    actionTimeout: 10_000,
    navigationTimeout: 30_000,
  },
  projects: [
    {
      name: 'admin',
      testMatch: /admin\/.+\.spec\.ts/,
      use: { baseURL: 'http://localhost:3006' },
    },
    {
      name: 'frontend',
      testMatch: /frontend\/.+\.spec\.ts/,
      use: { baseURL: 'http://localhost:5173' },
    },
    {
      name: 'cross-end',
      testMatch: /cross\/.+\.spec\.ts/,
      use: { baseURL: 'http://localhost:3006' },
    },
  ],
})
