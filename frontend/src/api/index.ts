import request from './request'

// ===== 公开接口 =====
/** 获取菜品分类 */
export const getCategories = () => request.get('/api/public/categories')

/** 获取菜品列表 */
export const getDishes = (params?: any) => request.get('/api/public/dishes', { params })

/** 获取菜品详情 */
export const getDishDetail = (id: number) => request.get(`/api/public/dishes/${id}`)

/** 获取可预订的桌台 */
export const getAvailableTables = () => request.get('/api/public/tables')

/** 提交预订 */
export const submitReservation = (data: any) => request.post('/api/public/reservations', data)

// ===== 会员接口 =====
/** 会员登录 */
export const memberLogin = (data: any) => request.post('/api/member/login', data)

/** 会员注册 */
export const memberRegister = (data: any) => request.post('/api/member/register', data)

/** 获取会员信息 */
export const getMemberInfo = () => request.get('/api/member/info')

/** 获取消费记录 */
export const getMyOrders = (params?: any) => request.get('/api/member/orders', { params })

/** 获取充值记录 */
export const getMyRecharges = (params?: any) => request.get('/api/member/recharges', { params })

/** 获取我的预订记录 */
export const getMyReservations = (params?: any) => request.get('/api/member/reservations', { params })
