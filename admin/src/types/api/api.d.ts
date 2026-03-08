/**
 * 火锅店管理系统 API 类型定义
 */
declare namespace Api {
  /** 通用类型 */
  namespace Common {
    interface PaginationParams {
      current: number
      size: number
      total: number
    }
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>
    interface PaginatedResponse<T = any> {
      records: T[]
      page: number
      size: number
      total: number
    }
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    interface LoginParams {
      username: string
      password: string
    }
    interface LoginResponse {
      token: string
      userId: number
      username: string
      realName: string
      role: string
      avatar: string | null
    }
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: number
      userName: string
      email: string
      avatar?: string
    }
  }

  /** 员工 */
  namespace Employee {
    interface Item {
      id: number
      username: string
      realName: string
      phone: string
      role: string
      avatar: string | null
      status: number
      createdAt: string
    }
  }

  /** 菜品分类 */
  namespace DishCategory {
    interface Item {
      id: number
      name: string
      sortOrder: number
      status: number
      createdAt: string
    }
  }

  /** 菜品 */
  namespace Dish {
    interface Item {
      id: number
      name: string
      categoryId: number
      categoryName: string
      price: number
      image: string | null
      description: string | null
      status: number
      spicyLevel: number
      isRecommended: number
      sortOrder: number
      createdAt: string
    }
  }

  /** 桌台 */
  namespace DiningTable {
    interface Item {
      id: number
      tableNumber: string
      capacity: number
      status: number
      area: string | null
      createdAt: string
    }
  }

  /** 预订 */
  namespace Reservation {
    interface Item {
      id: number
      tableId: number
      tableNumber: string
      customerName: string
      customerPhone: string
      reservationTime: string
      guestCount: number
      status: number
      remark: string | null
      createdAt: string
    }
  }

  /** 订单 */
  namespace Order {
    interface Item {
      id: number
      orderNo: string
      tableId: number | null
      tableNumber: string | null
      memberId: number | null
      memberName: string | null
      orderStatus: number
      totalAmount: number
      discountAmount: number
      actualAmount: number
      guestCount: number
      remark: string | null
      createdByName: string | null
      createdAt: string
      items?: OrderItem[]
    }
    interface OrderItem {
      id: number
      orderId: number
      dishId: number
      dishName: string
      dishPrice: number
      quantity: number
      subtotal: number
      remark: string | null
      status: number
    }
  }

  /** 支付 */
  namespace Payment {
    interface Item {
      id: number
      orderId: number
      orderNo: string
      paymentNo: string
      paymentMethod: number
      amount: number
      status: number
      cashierName: string | null
      createdAt: string
    }
  }

  /** 食材 */
  namespace Ingredient {
    interface Item {
      id: number
      name: string
      unit: string
      stockQuantity: number
      warningQuantity: number
      category: string | null
      price: number | null
      supplier: string | null
      status: number
      createdAt: string
    }
  }

  /** 出入库记录 */
  namespace InventoryRecord {
    interface Item {
      id: number
      ingredientId: number
      ingredientName: string
      type: number
      quantity: number
      beforeQuantity: number
      afterQuantity: number
      unitPrice: number | null
      totalPrice: number | null
      remark: string | null
      operatorName: string | null
      createdAt: string
    }
  }

  /** 会员 */
  namespace Member {
    interface Item {
      id: number
      memberNo: string
      name: string
      phone: string
      gender: number | null
      birthday: string | null
      levelId: number
      levelName: string
      balance: number
      points: number
      totalConsumption: number
      status: number
      createdAt: string
    }
  }

  /** 会员等级 */
  namespace MemberLevel {
    interface Item {
      id: number
      name: string
      minPoints: number
      discount: number
      description: string | null
    }
  }

  /** 会员充值 */
  namespace MemberRecharge {
    interface Item {
      id: number
      memberId: number
      memberName: string
      amount: number
      giftAmount: number
      balanceAfter: number
      paymentMethod: number
      operatorName: string | null
      createdAt: string
    }
  }

  /** 操作日志 */
  namespace OperationLog {
    interface Item {
      id: number
      operatorId: number
      operatorName: string
      module: string
      action: string
      detail: string
      ip: string
      createdAt: string
    }
  }

  /** 数据面板 */
  namespace Dashboard {
    interface Data {
      todayRevenue: number
      todayOrders: number
      totalMembers: number
      lowStockCount: number
      tableStatus: { free: number; occupied: number; reserved: number }
      revenueTrend: { date: string; revenue: number }[]
      dishSalesTop: { name: string; sales: number }[]
    }
  }
}
