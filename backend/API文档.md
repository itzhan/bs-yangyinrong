# 火锅店管理系统 API 文档

## 概览
- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Token（Bearer Token）
- **Content-Type**: `application/json`
- **分页参数**: `page`（页码，从1开始）、`size`（每页数量，默认10）

## 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 分页响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

## 错误码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未认证/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证模块

### 1.1 登录
- **POST** `/api/auth/login`
- **权限**: 公开
- **请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```
- **响应**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "张伟",
    "role": "ADMIN",
    "avatar": null
  }
}
```

---

## 2. 员工管理（仅管理员）

### 2.1 员工列表
- **GET** `/api/admin/employees?page=1&size=10&keyword=&role=`
- **权限**: ADMIN
- **参数**: `keyword`(姓名/用户名/手机号), `role`(ADMIN/CASHIER/KITCHEN/INVENTORY)

### 2.2 员工详情
- **GET** `/api/admin/employees/{id}`

### 2.3 新增员工
- **POST** `/api/admin/employees`
```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "新员工",
  "phone": "13800000000",
  "role": "CASHIER"
}
```

### 2.4 更新员工
- **PUT** `/api/admin/employees/{id}`
```json
{
  "username": "newuser",
  "realName": "修改后",
  "phone": "13800000001",
  "role": "CASHIER",
  "status": 1
}
```

### 2.5 删除员工
- **DELETE** `/api/admin/employees/{id}`

### 2.6 重置密码
- **PUT** `/api/admin/employees/{id}/password`
```json
{ "password": "newpass123" }
```

---

## 3. 菜品分类管理

### 3.1 分类列表
- **GET** `/api/admin/categories?keyword=`
- **权限**: ADMIN
- **返回**: 数组（非分页）

### 3.2 分类详情
- **GET** `/api/admin/categories/{id}`

### 3.3 新增分类
- **POST** `/api/admin/categories`
```json
{ "name": "锅底", "sortOrder": 1, "status": 1 }
```

### 3.4 更新分类
- **PUT** `/api/admin/categories/{id}`

### 3.5 删除分类
- **DELETE** `/api/admin/categories/{id}`

---

## 4. 菜品管理

### 4.1 菜品列表
- **GET** `/api/admin/dishes?page=1&size=10&keyword=&categoryId=&status=`
- **权限**: ADMIN

### 4.2 菜品详情
- **GET** `/api/admin/dishes/{id}`

### 4.3 新增菜品
- **POST** `/api/admin/dishes`
```json
{
  "name": "经典麻辣锅底",
  "categoryId": 1,
  "price": 68.00,
  "image": "/uploads/xxx.jpg",
  "description": "精选二十余种香料",
  "status": 1,
  "spicyLevel": 3,
  "isRecommended": 1,
  "sortOrder": 1
}
```

### 4.4 更新菜品
- **PUT** `/api/admin/dishes/{id}`

### 4.5 删除菜品
- **DELETE** `/api/admin/dishes/{id}`

### 4.6 更新菜品状态
- **PUT** `/api/admin/dishes/{id}/status/{status}`
- status: 0-下架, 1-在售, 2-售罄

---

## 5. 桌台管理

### 5.1 桌台列表
- **GET** `/api/admin/tables?area=&status=`
- **返回**: 数组（非分页）

### 5.2 桌台详情
- **GET** `/api/admin/tables/{id}`

### 5.3 新增桌台
- **POST** `/api/admin/tables`
```json
{ "tableNumber": "A01", "capacity": 4, "area": "大厅" }
```

### 5.4 更新桌台
- **PUT** `/api/admin/tables/{id}`

### 5.5 删除桌台
- **DELETE** `/api/admin/tables/{id}`

### 5.6 更新桌台状态
- **PUT** `/api/admin/tables/{id}/status/{status}`
- status: 0-空闲, 1-占用, 2-预订

---

## 6. 桌台预订管理

### 6.1 预订列表
- **GET** `/api/admin/reservations?page=1&size=10&status=&keyword=`

### 6.2 预订详情
- **GET** `/api/admin/reservations/{id}`

### 6.3 新增预订
- **POST** `/api/admin/reservations`
```json
{
  "tableId": 1,
  "customerName": "王先生",
  "customerPhone": "13900139001",
  "reservationTime": "2026-02-08 18:00:00",
  "guestCount": 4,
  "remark": "生日聚餐"
}
```

### 6.4 更新预订
- **PUT** `/api/admin/reservations/{id}`

### 6.5 取消预订
- **PUT** `/api/admin/reservations/{id}/cancel`

### 6.6 确认到店
- **PUT** `/api/admin/reservations/{id}/arrive`

---

## 7. 订单管理

### 7.1 订单列表
- **GET** `/api/admin/orders?page=1&size=10&orderStatus=&keyword=`
- orderStatus: 0-待确认, 1-已确认(制作中), 2-已完成(待结算), 3-已结算, 4-已取消

### 7.2 订单详情
- **GET** `/api/admin/orders/{id}`
- **响应**（含订单项列表）:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "orderNo": "ORD20260207001",
    "tableId": 1,
    "tableNumber": "A01",
    "memberId": 1,
    "memberName": "刘晓红",
    "orderStatus": 1,
    "totalAmount": 286.00,
    "discountAmount": 28.60,
    "actualAmount": 257.40,
    "guestCount": 3,
    "items": [
      {
        "id": 1,
        "dishId": 1,
        "dishName": "经典麻辣锅底",
        "dishPrice": 68.00,
        "quantity": 1,
        "subtotal": 68.00,
        "status": 0
      }
    ],
    "createdAt": "2026-02-07 12:00:00"
  }
}
```

### 7.3 创建订单
- **POST** `/api/admin/orders`
```json
{
  "tableId": 1,
  "memberId": null,
  "guestCount": 3,
  "remark": "",
  "items": [
    { "dishId": 1, "quantity": 1, "remark": "" },
    { "dishId": 6, "quantity": 2, "remark": "多切薄" }
  ]
}
```

### 7.4 加菜
- **POST** `/api/admin/orders/{id}/items`
```json
[
  { "dishId": 9, "quantity": 1, "remark": "" }
]
```

### 7.5 确认订单
- **PUT** `/api/admin/orders/{id}/confirm`

### 7.6 完成订单
- **PUT** `/api/admin/orders/{id}/complete`

### 7.7 取消订单
- **PUT** `/api/admin/orders/{id}/cancel`

---

## 8. 收银结算

### 8.1 支付记录列表
- **GET** `/api/admin/payments?page=1&size=10&paymentMethod=&keyword=`
- paymentMethod: 0-现金, 1-微信, 2-支付宝, 3-银行卡, 4-会员余额

### 8.2 支付详情
- **GET** `/api/admin/payments/{id}`

### 8.3 结算
- **POST** `/api/admin/payments`
```json
{ "orderId": 1, "paymentMethod": 1 }
```

### 8.4 退款
- **PUT** `/api/admin/payments/{id}/refund`

---

## 9. 库存管理

### 9.1 食材列表
- **GET** `/api/admin/ingredients?page=1&size=10&keyword=&category=`

### 9.2 食材详情
- **GET** `/api/admin/ingredients/{id}`

### 9.3 新增食材
- **POST** `/api/admin/ingredients`
```json
{
  "name": "肥牛卷",
  "unit": "kg",
  "stockQuantity": 50.00,
  "warningQuantity": 10.00,
  "category": "肉类",
  "price": 85.00,
  "supplier": "海底捞供应链"
}
```

### 9.4 更新食材
- **PUT** `/api/admin/ingredients/{id}`

### 9.5 删除食材
- **DELETE** `/api/admin/ingredients/{id}`

### 9.6 低库存预警
- **GET** `/api/admin/ingredients/warning`
- **返回**: 数组

### 9.7 出入库/盘点操作
- **POST** `/api/admin/ingredients/operate`
```json
{
  "ingredientId": 1,
  "type": 0,
  "quantity": 30.00,
  "unitPrice": 85.00,
  "remark": "每周常规采购"
}
```
- type: 0-入库, 1-出库, 2-盘点

### 9.8 出入库记录
- **GET** `/api/admin/ingredients/records?page=1&size=10&ingredientId=&type=`

---

## 10. 会员管理

### 10.1 会员列表
- **GET** `/api/admin/members?page=1&size=10&keyword=&levelId=`

### 10.2 会员详情
- **GET** `/api/admin/members/{id}`

### 10.3 新增会员
- **POST** `/api/admin/members`
```json
{
  "name": "张三",
  "phone": "18600001001",
  "gender": 1,
  "birthday": "1990-01-01"
}
```

### 10.4 更新会员
- **PUT** `/api/admin/members/{id}`

### 10.5 删除会员
- **DELETE** `/api/admin/members/{id}`

### 10.6 会员充值
- **POST** `/api/admin/members/recharge`
```json
{
  "memberId": 1,
  "amount": 500.00,
  "giftAmount": 50.00,
  "paymentMethod": 1
}
```

### 10.7 充值记录
- **GET** `/api/admin/members/recharges?page=1&size=10&memberId=`

### 10.8 会员等级列表
- **GET** `/api/admin/members/levels`

### 10.9 新增等级
- **POST** `/api/admin/members/levels`
```json
{
  "name": "银卡会员",
  "minPoints": 500,
  "discount": 0.95,
  "description": "累计积分满500享95折"
}
```

### 10.10 更新等级
- **PUT** `/api/admin/members/levels/{id}`

### 10.11 删除等级
- **DELETE** `/api/admin/members/levels/{id}`

---

## 11. 数据统计

### 11.1 仪表盘数据
- **GET** `/api/admin/dashboard`
- **响应**:
```json
{
  "code": 200,
  "data": {
    "todayRevenue": 1580.50,
    "todayOrders": 12,
    "totalMembers": 15,
    "lowStockCount": 3,
    "tableStatus": { "free": 10, "occupied": 3, "reserved": 2 },
    "revenueTrend": [
      { "date": "2026-02-01", "revenue": 938.20 },
      { "date": "2026-02-02", "revenue": 1023.30 }
    ],
    "dishSalesTop": [
      { "name": "精品肥牛卷", "sales": 28 },
      { "name": "毛肚", "sales": 22 }
    ]
  }
}
```

---

## 12. 操作日志

### 12.1 日志列表
- **GET** `/api/admin/logs?page=1&size=10&module=&keyword=`

---

## 13. 后厨端

### 13.1 后厨订单列表
- **GET** `/api/kitchen/orders?page=1&size=20&status=`
- **权限**: ADMIN, KITCHEN

### 13.2 确认订单（开始制作）
- **PUT** `/api/kitchen/orders/{id}/confirm`

### 13.3 完成订单
- **PUT** `/api/kitchen/orders/{id}/complete`

### 13.4 更新订单项制作状态
- **PUT** `/api/kitchen/items/{itemId}/status/{status}`
- status: 0-待制作, 1-制作中, 2-已完成, 3-已上菜

---

## 14. 库管端

### 14.1 食材列表
- **GET** `/api/inventory/ingredients?page=1&size=10&keyword=&category=`
- **权限**: ADMIN, INVENTORY

### 14.2 低库存预警
- **GET** `/api/inventory/warning`

### 14.3 出入库操作
- **POST** `/api/inventory/operate`

### 14.4 出入库记录
- **GET** `/api/inventory/records?page=1&size=10&ingredientId=&type=`

---

## 15. 文件上传

### 15.1 上传文件
- **POST** `/api/file/upload`
- **Content-Type**: multipart/form-data
- **参数**: `file` (MultipartFile)
- **响应**: `{ "code": 200, "data": "/uploads/xxx.jpg" }`

---

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 店长/管理员 | admin | 123456 | 全权限 |
| 收银员 | cashier1 | 123456 | 收银、订单、会员 |
| 后厨 | kitchen1 | 123456 | 接单、制作状态 |
| 库管员 | inventory1 | 123456 | 库存管理 |
