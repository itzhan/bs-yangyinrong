import request from '@/utils/http'

// ==================== 仪表盘 ====================
export function fetchDashboard() {
  return request.get<Api.Dashboard.Data>({ url: '/api/admin/dashboard' })
}

// ==================== 员工管理 ====================
export function fetchEmployeeList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Employee.Item>>({
    url: '/api/admin/employees',
    params
  })
}
export function fetchEmployeeDetail(id: number) {
  return request.get<Api.Employee.Item>({ url: `/api/admin/employees/${id}` })
}
export function createEmployee(data: any) {
  return request.post<void>({ url: '/api/admin/employees', params: data, showSuccessMessage: true })
}
export function updateEmployee(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/employees/${id}`, params: data, showSuccessMessage: true })
}
export function deleteEmployee(id: number) {
  return request.del<void>({ url: `/api/admin/employees/${id}`, showSuccessMessage: true })
}
export function resetEmployeePassword(id: number, password: string) {
  return request.put<void>({ url: `/api/admin/employees/${id}/password`, params: { password }, showSuccessMessage: true })
}

// ==================== 菜品分类 ====================
export function fetchCategoryList(params?: any) {
  return request.get<Api.DishCategory.Item[]>({ url: '/api/admin/categories', params })
}
export function createCategory(data: any) {
  return request.post<void>({ url: '/api/admin/categories', params: data, showSuccessMessage: true })
}
export function updateCategory(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/categories/${id}`, params: data, showSuccessMessage: true })
}
export function deleteCategory(id: number) {
  return request.del<void>({ url: `/api/admin/categories/${id}`, showSuccessMessage: true })
}

// ==================== 菜品管理 ====================
export function fetchDishList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Dish.Item>>({
    url: '/api/admin/dishes',
    params
  })
}
export function fetchDishDetail(id: number) {
  return request.get<Api.Dish.Item>({ url: `/api/admin/dishes/${id}` })
}
export function createDish(data: any) {
  return request.post<void>({ url: '/api/admin/dishes', params: data, showSuccessMessage: true })
}
export function updateDish(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/dishes/${id}`, params: data, showSuccessMessage: true })
}
export function deleteDish(id: number) {
  return request.del<void>({ url: `/api/admin/dishes/${id}`, showSuccessMessage: true })
}
export function updateDishStatus(id: number, status: number) {
  return request.put<void>({ url: `/api/admin/dishes/${id}/status/${status}`, showSuccessMessage: true })
}

// ==================== 桌台管理 ====================
export function fetchTableList(params?: any) {
  return request.get<Api.DiningTable.Item[]>({ url: '/api/admin/tables', params })
}
export function createTable(data: any) {
  return request.post<void>({ url: '/api/admin/tables', params: data, showSuccessMessage: true })
}
export function updateTable(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/tables/${id}`, params: data, showSuccessMessage: true })
}
export function deleteTable(id: number) {
  return request.del<void>({ url: `/api/admin/tables/${id}`, showSuccessMessage: true })
}
export function updateTableStatus(id: number, status: number) {
  return request.put<void>({ url: `/api/admin/tables/${id}/status/${status}`, showSuccessMessage: true })
}

// ==================== 桌台预订 ====================
export function fetchReservationList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Reservation.Item>>({
    url: '/api/admin/reservations',
    params
  })
}
export function createReservation(data: any) {
  return request.post<void>({ url: '/api/admin/reservations', params: data, showSuccessMessage: true })
}
export function updateReservation(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/reservations/${id}`, params: data, showSuccessMessage: true })
}
export function cancelReservation(id: number) {
  return request.put<void>({ url: `/api/admin/reservations/${id}/cancel`, showSuccessMessage: true })
}
export function arriveReservation(id: number) {
  return request.put<void>({ url: `/api/admin/reservations/${id}/arrive`, showSuccessMessage: true })
}

// ==================== 订单管理 ====================
export function fetchOrderList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Order.Item>>({
    url: '/api/admin/orders',
    params
  })
}
export function fetchOrderDetail(id: number) {
  return request.get<Api.Order.Item>({ url: `/api/admin/orders/${id}` })
}
export function createOrder(data: any) {
  return request.post<Api.Order.Item>({ url: '/api/admin/orders', params: data, showSuccessMessage: true })
}
export function addOrderItems(id: number, items: any[]) {
  return request.post<void>({ url: `/api/admin/orders/${id}/items`, params: items, showSuccessMessage: true })
}
export function confirmOrder(id: number) {
  return request.put<void>({ url: `/api/admin/orders/${id}/confirm`, showSuccessMessage: true })
}
export function completeOrder(id: number) {
  return request.put<void>({ url: `/api/admin/orders/${id}/complete`, showSuccessMessage: true })
}
export function cancelOrder(id: number) {
  return request.put<void>({ url: `/api/admin/orders/${id}/cancel`, showSuccessMessage: true })
}

// ==================== 收银结算 ====================
export function fetchPaymentList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Payment.Item>>({
    url: '/api/admin/payments',
    params
  })
}
export function createPayment(data: any) {
  return request.post<Api.Payment.Item>({ url: '/api/admin/payments', params: data, showSuccessMessage: true })
}
export function refundPayment(id: number) {
  return request.put<void>({ url: `/api/admin/payments/${id}/refund`, showSuccessMessage: true })
}

// ==================== 库存管理 ====================
export function fetchIngredientList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Ingredient.Item>>({
    url: '/api/admin/ingredients',
    params
  })
}
export function createIngredient(data: any) {
  return request.post<void>({ url: '/api/admin/ingredients', params: data, showSuccessMessage: true })
}
export function updateIngredient(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/ingredients/${id}`, params: data, showSuccessMessage: true })
}
export function deleteIngredient(id: number) {
  return request.del<void>({ url: `/api/admin/ingredients/${id}`, showSuccessMessage: true })
}
export function fetchLowStockWarning() {
  return request.get<Api.Ingredient.Item[]>({ url: '/api/admin/ingredients/warning' })
}
export function operateInventory(data: any) {
  return request.post<void>({ url: '/api/admin/ingredients/operate', params: data, showSuccessMessage: true })
}
export function fetchInventoryRecords(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.InventoryRecord.Item>>({
    url: '/api/admin/ingredients/records',
    params
  })
}

// ==================== 会员管理 ====================
export function fetchMemberList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.Member.Item>>({
    url: '/api/admin/members',
    params
  })
}
export function fetchMemberDetail(id: number) {
  return request.get<Api.Member.Item>({ url: `/api/admin/members/${id}` })
}
export function createMember(data: any) {
  return request.post<void>({ url: '/api/admin/members', params: data, showSuccessMessage: true })
}
export function updateMember(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/members/${id}`, params: data, showSuccessMessage: true })
}
export function deleteMember(id: number) {
  return request.del<void>({ url: `/api/admin/members/${id}`, showSuccessMessage: true })
}
export function rechargeMember(data: any) {
  return request.post<void>({ url: '/api/admin/members/recharge', params: data, showSuccessMessage: true })
}
export function fetchRechargeList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.MemberRecharge.Item>>({
    url: '/api/admin/members/recharges',
    params
  })
}
export function fetchMemberLevels() {
  return request.get<Api.MemberLevel.Item[]>({ url: '/api/admin/members/levels' })
}
export function createMemberLevel(data: any) {
  return request.post<void>({ url: '/api/admin/members/levels', params: data, showSuccessMessage: true })
}
export function updateMemberLevel(id: number, data: any) {
  return request.put<void>({ url: `/api/admin/members/levels/${id}`, params: data, showSuccessMessage: true })
}
export function deleteMemberLevel(id: number) {
  return request.del<void>({ url: `/api/admin/members/levels/${id}`, showSuccessMessage: true })
}

// ==================== 操作日志 ====================
export function fetchLogList(params: any) {
  return request.get<Api.Common.PaginatedResponse<Api.OperationLog.Item>>({
    url: '/api/admin/logs',
    params
  })
}
