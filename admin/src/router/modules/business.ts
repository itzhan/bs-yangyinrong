import { AppRouteRecord } from '@/types/router'

export const businessRoutes: AppRouteRecord = {
  path: '/business',
  name: 'Business',
  redirect: '/business/dishes',
  component: '/index/index',
  meta: {
    title: '业务管理',
    icon: 'ri:restaurant-line',
    roles: ['ROLE_ADMIN', 'ROLE_CASHIER']
  },
  children: [
    {
      path: 'categories',
      name: 'CategoryManage',
      component: '/business/category',
      meta: { title: '菜品分类', icon: 'ri:folder-line', roles: ['ROLE_ADMIN'] }
    },
    {
      path: 'dishes',
      name: 'DishManage',
      component: '/business/dish',
      meta: { title: '菜品管理', icon: 'ri:restaurant-2-line', roles: ['ROLE_ADMIN'] }
    },
    {
      path: 'tables',
      name: 'TableManage',
      component: '/business/table',
      meta: { title: '桌台管理', icon: 'ri:layout-grid-line', roles: ['ROLE_ADMIN', 'ROLE_CASHIER'] }
    },
    {
      path: 'reservations',
      name: 'ReservationManage',
      component: '/business/reservation',
      meta: { title: '桌台预订', icon: 'ri:calendar-check-line', roles: ['ROLE_ADMIN', 'ROLE_CASHIER'] }
    },
    {
      path: 'orders',
      name: 'OrderManage',
      component: '/business/order',
      meta: { title: '订单管理', icon: 'ri:file-list-3-line', roles: ['ROLE_ADMIN', 'ROLE_CASHIER'] }
    },
    {
      path: 'payments',
      name: 'PaymentManage',
      component: '/business/payment',
      meta: { title: '收银结算', icon: 'ri:money-cny-circle-line', roles: ['ROLE_ADMIN', 'ROLE_CASHIER'] }
    },
    {
      path: 'members',
      name: 'MemberManage',
      component: '/business/member',
      meta: { title: '会员管理', icon: 'ri:vip-crown-line', roles: ['ROLE_ADMIN', 'ROLE_CASHIER'] }
    },
    {
      path: 'ingredients',
      name: 'IngredientManage',
      component: '/business/ingredient',
      meta: { title: '库存管理', icon: 'ri:stock-line', roles: ['ROLE_ADMIN'] }
    },
    {
      path: 'employees',
      name: 'EmployeeManage',
      component: '/business/employee',
      meta: { title: '员工管理', icon: 'ri:team-line', roles: ['ROLE_ADMIN'] }
    },
    {
      path: 'logs',
      name: 'LogManage',
      component: '/business/log',
      meta: { title: '操作日志', icon: 'ri:file-text-line', roles: ['ROLE_ADMIN'] }
    }
  ]
}
