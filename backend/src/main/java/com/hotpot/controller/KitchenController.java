package com.hotpot.controller;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.entity.Order;
import com.hotpot.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final OrderService orderService;

    /** 后厨订单列表(待处理/制作中) */
    @GetMapping("/orders")
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.kitchenOrders(page, size, status));
    }

    /** 确认订单(开始制作) */
    @PutMapping("/orders/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return Result.success();
    }

    /** 完成订单 */
    @PutMapping("/orders/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    /** 更新订单项制作状态 */
    @PutMapping("/items/{itemId}/status/{status}")
    public Result<Void> updateItemStatus(@PathVariable Long itemId, @PathVariable Integer status) {
        orderService.updateItemStatus(itemId, status);
        return Result.success();
    }
}
