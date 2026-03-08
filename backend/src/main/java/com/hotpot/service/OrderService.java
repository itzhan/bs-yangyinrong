package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.OrderDTO;
import com.hotpot.entity.Order;

public interface OrderService {
    PageResult<Order> list(Integer page, Integer size, Integer orderStatus, String keyword);
    Order getById(Long id);
    Order create(OrderDTO dto);
    void addItems(Long orderId, java.util.List<OrderDTO.OrderItemDTO> items);
    void confirm(Long id);
    void complete(Long id);
    void cancel(Long id);
    /** 后厨: 获取待处理/制作中的订单 */
    PageResult<Order> kitchenOrders(Integer page, Integer size, Integer status);
    /** 后厨: 更新订单项状态 */
    void updateItemStatus(Long itemId, Integer status);
}
