import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: () => import('@/views/home/index.vue') },
    { path: '/menu', name: 'Menu', component: () => import('@/views/menu/index.vue') },
    { path: '/reservation', name: 'Reservation', component: () => import('@/views/reservation/index.vue') },
    { path: '/member/login', name: 'MemberLogin', component: () => import('@/views/member/login.vue'), meta: { hideLayout: true } },
    { path: '/member/register', name: 'MemberRegister', component: () => import('@/views/member/register.vue'), meta: { hideLayout: true } },
    { path: '/member', name: 'MemberCenter', component: () => import('@/views/member/index.vue'), meta: { requireAuth: true } },
    { path: '/about', name: 'About', component: () => import('@/views/about/index.vue') }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  if (to.meta.requireAuth) {
    const token = localStorage.getItem('member_token')
    if (!token) {
      next({ name: 'MemberLogin', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
