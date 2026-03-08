package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.common.SecurityUtils;
import com.hotpot.dto.OrderDTO;
import com.hotpot.entity.*;
import com.hotpot.mapper.*;
import com.hotpot.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final DiningTableMapper tableMapper;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    public PageResult<Order> list(Integer page, Integer size, Integer orderStatus, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (orderStatus != null) {
            wrapper.eq(Order::getOrderStatus, orderStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword));
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        fillOrderDetails(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        fillOrderDetails(List.of(order));
        // 加载订单项
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        order.setItems(items);
        return order;
    }

    @Override
    @Transactional
    public Order create(OrderDTO dto) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setTableId(dto.getTableId());
        order.setMemberId(dto.getMemberId());
        order.setGuestCount(dto.getGuestCount());
        order.setRemark(dto.getRemark());
        order.setOrderStatus(0);
        order.setCreatedBy(SecurityUtils.getCurrentUserId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null) throw new BusinessException("菜品不存在: " + itemDTO.getDishId());
            if (dish.getStatus() != 1) throw new BusinessException("菜品已下架或售罄: " + dish.getName());
            totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // 计算会员折扣
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (dto.getMemberId() != null) {
            Member member = memberMapper.selectById(dto.getMemberId());
            if (member != null) {
                MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
                if (level != null && level.getDiscount().compareTo(BigDecimal.ONE) < 0) {
                    discountAmount = totalAmount.subtract(totalAmount.multiply(level.getDiscount()));
                }
            }
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(totalAmount.subtract(discountAmount));
        orderMapper.insert(order);

        // 创建订单项
        for (OrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishPrice(dish.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(dish.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            item.setRemark(itemDTO.getRemark());
            item.setStatus(0);
            orderItemMapper.insert(item);
        }

        // 更新桌台状态为占用
        if (dto.getTableId() != null) {
            DiningTable table = tableMapper.selectById(dto.getTableId());
            if (table != null) {
                table.setStatus(1);
                tableMapper.updateById(table);
            }
        }

        return order;
    }

    @Override
    @Transactional
    public void addItems(Long orderId, List<OrderDTO.OrderItemDTO> items) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getOrderStatus() >= 2) throw new BusinessException("订单已完成，不能加菜");

        BigDecimal addAmount = BigDecimal.ZERO;
        for (OrderDTO.OrderItemDTO itemDTO : items) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null) throw new BusinessException("菜品不存在");
            if (dish.getStatus() != 1) throw new BusinessException("菜品已下架或售罄: " + dish.getName());

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishPrice(dish.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(dish.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            item.setRemark(itemDTO.getRemark());
            item.setStatus(0);
            orderItemMapper.insert(item);
            addAmount = addAmount.add(item.getSubtotal());
        }

        // 重新计算金额
        order.setTotalAmount(order.getTotalAmount().add(addAmount));
        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
                if (level != null && level.getDiscount().compareTo(BigDecimal.ONE) < 0) {
                    order.setDiscountAmount(order.getTotalAmount().subtract(
                            order.getTotalAmount().multiply(level.getDiscount())));
                }
            }
        }
        order.setActualAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));
        orderMapper.updateById(order);
    }

    @Override
    public void confirm(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getOrderStatus() != 0) throw new BusinessException("只有待确认的订单可以确认");
        order.setOrderStatus(1);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getOrderStatus() != 1) throw new BusinessException("只有制作中的订单可以完成");
        order.setOrderStatus(2);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (order.getOrderStatus() >= 3) throw new BusinessException("订单已结算或已取消");
        order.setOrderStatus(4);
        orderMapper.updateById(order);

        // 释放桌台
        if (order.getTableId() != null) {
            DiningTable table = tableMapper.selectById(order.getTableId());
            if (table != null && table.getStatus() == 1) {
                table.setStatus(0);
                tableMapper.updateById(table);
            }
        }
    }

    @Override
    public PageResult<Order> kitchenOrders(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getOrderStatus, status);
        } else {
            wrapper.in(Order::getOrderStatus, 0, 1);
        }
        wrapper.orderByAsc(Order::getCreatedAt);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        // 加载每个订单的菜品项
        for (Order order : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            order.setItems(items);
            if (order.getTableId() != null) {
                DiningTable table = tableMapper.selectById(order.getTableId());
                if (table != null) order.setTableNumber(table.getTableNumber());
            }
        }
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void updateItemStatus(Long itemId, Integer status) {
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException(404, "订单项不存在");
        item.setStatus(status);
        orderItemMapper.updateById(item);
    }

    private void fillOrderDetails(List<Order> orders) {
        if (orders.isEmpty()) return;
        List<DiningTable> tables = tableMapper.selectList(null);
        Map<Long, String> tableMap = tables.stream()
                .collect(Collectors.toMap(DiningTable::getId, DiningTable::getTableNumber));
        List<Member> members = memberMapper.selectList(null);
        Map<Long, String> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));
        List<SysEmployee> employees = employeeMapper.selectList(null);
        Map<Long, String> empMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getRealName));

        for (Order order : orders) {
            if (order.getTableId() != null) order.setTableNumber(tableMap.get(order.getTableId()));
            if (order.getMemberId() != null) order.setMemberName(memberMap.get(order.getMemberId()));
            if (order.getCreatedBy() != null) order.setCreatedByName(empMap.get(order.getCreatedBy()));
        }
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", (int) (Math.random() * 1000));
    }
}
