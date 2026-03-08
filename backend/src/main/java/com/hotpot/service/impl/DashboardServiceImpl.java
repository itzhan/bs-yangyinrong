package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.entity.*;
import com.hotpot.mapper.*;
import com.hotpot.service.DashboardService;
import com.hotpot.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;
    private final IngredientMapper ingredientMapper;
    private final DiningTableMapper tableMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 今日营收
        List<Payment> todayPayments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1)
                        .between(Payment::getCreatedAt, todayStart, todayEnd));
        BigDecimal todayRevenue = todayPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayRevenue(todayRevenue);

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .between(Order::getCreatedAt, todayStart, todayEnd)
                        .ne(Order::getOrderStatus, 4));
        vo.setTodayOrders(todayOrders);

        // 会员总数
        vo.setTotalMembers(memberMapper.selectCount(null));

        // 低库存预警
        vo.setLowStockCount(ingredientMapper.selectCount(
                new LambdaQueryWrapper<Ingredient>()
                        .apply("stock_quantity <= warning_quantity")
                        .eq(Ingredient::getStatus, 1)));

        // 桌台状态
        List<DiningTable> tables = tableMapper.selectList(null);
        Map<String, Long> tableStatus = new HashMap<>();
        tableStatus.put("free", tables.stream().filter(t -> t.getStatus() == 0).count());
        tableStatus.put("occupied", tables.stream().filter(t -> t.getStatus() == 1).count());
        tableStatus.put("reserved", tables.stream().filter(t -> t.getStatus() == 2).count());
        vo.setTableStatus(tableStatus);

        // 最近7天营收趋势
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Payment> payments = paymentMapper.selectList(
                    new LambdaQueryWrapper<Payment>()
                            .eq(Payment::getStatus, 1)
                            .between(Payment::getCreatedAt, date.atStartOfDay(), date.atTime(LocalTime.MAX)));
            BigDecimal revenue = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("revenue", revenue);
            revenueTrend.add(item);
        }
        vo.setRevenueTrend(revenueTrend);

        // 菜品销量TOP10（按最近30天）
        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();
        List<Order> recentOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreatedAt, thirtyDaysAgo)
                        .ne(Order::getOrderStatus, 4));
        Set<Long> orderIds = recentOrders.stream().map(Order::getId).collect(Collectors.toSet());
        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            Map<String, Integer> salesMap = items.stream()
                    .collect(Collectors.groupingBy(OrderItem::getDishName, Collectors.summingInt(OrderItem::getQuantity)));
            List<Map<String, Object>> dishSalesTop = salesMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .map(e -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("name", e.getKey());
                        m.put("sales", e.getValue());
                        return m;
                    })
                    .collect(Collectors.toList());
            vo.setDishSalesTop(dishSalesTop);
        } else {
            vo.setDishSalesTop(new ArrayList<>());
        }

        return vo;
    }
}
