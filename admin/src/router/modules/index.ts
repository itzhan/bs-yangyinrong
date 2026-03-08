import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { systemRoutes } from './system'
import { businessRoutes } from './business'
import { exceptionRoutes } from './exception'

/**
 * 导出所有模块化路由 - 已清理demo路由，仅保留业务路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  businessRoutes,
  systemRoutes,
  exceptionRoutes
]
