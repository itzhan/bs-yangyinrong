package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.OrderDTO;
import com.hotpot.entity.Order;
import com.hotpot.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) String keyword) {
        return Result.success(orderService.list(page, size, orderStatus, keyword));
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderDTO dto) {
        return Result.success(orderService.create(dto));
    }

    @PostMapping("/{id}/items")
    public Result<Void> addItems(@PathVariable Long id, @Valid @RequestBody List<OrderDTO.OrderItemDTO> items) {
        orderService.addItems(id, items);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }
}
