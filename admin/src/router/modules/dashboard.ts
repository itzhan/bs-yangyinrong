import { AppRouteRecord } from '@/types/router'

export const dashboardRoutes: AppRouteRecord = {
  name: 'Dashboard',
  path: '/dashboard',
  redirect: '/dashboard/home',
  component: '/index/index',
  meta: {
    title: '数据面板',
    icon: 'ri:pie-chart-line',
    roles: ['ROLE_ADMIN', 'ROLE_CASHIER']
  },
  children: [
    {
      path: 'home',
      name: 'DashboardHome',
      component: '/dashboard/home',
      meta: {
        title: '运营概览',
        icon: 'ri:home-smile-2-line',
        keepAlive: false,
        fixedTab: true
      }
    }
  ]
}
