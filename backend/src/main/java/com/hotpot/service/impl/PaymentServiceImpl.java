package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.common.SecurityUtils;
import com.hotpot.dto.PaymentDTO;
import com.hotpot.entity.*;
import com.hotpot.mapper.*;
import com.hotpot.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final DiningTableMapper tableMapper;
    private final MemberMapper memberMapper;
    private final SysEmployeeMapper employeeMapper;
    private final DishIngredientMapper dishIngredientMapper;
    private final IngredientMapper ingredientMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public PageResult<Payment> list(Integer page, Integer size, Integer paymentMethod, String keyword) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (paymentMethod != null) {
            wrapper.eq(Payment::getPaymentMethod, paymentMethod);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Payment::getPaymentNo, keyword);
        }
        wrapper.orderByDesc(Payment::getCreatedAt);
        Page<Payment> result = paymentMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充关联信息
        List<Order> orders = orderMapper.selectList(null);
        Map<Long, String> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, Order::getOrderNo));
        List<SysEmployee> employees = employeeMapper.selectList(null);
        Map<Long, String> empMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getRealName));

        result.getRecords().forEach(p -> {
            p.setOrderNo(orderMap.get(p.getOrderId()));
            if (p.getCashierId() != null) p.setCashierName(empMap.get(p.getCashierId()));
        });

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Payment getById(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException(404, "支付记录不存在");
        return payment;
    }

    @Override
    @Transactional
    public Payment pay(PaymentDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getOrderStatus() == 3) throw new BusinessException("订单已结算");
        if (order.getOrderStatus() == 4) throw new BusinessException("订单已取消");

        // 会员余额支付
        if (dto.getPaymentMethod() == 4 && order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member == null) throw new BusinessException("会员不存在");
            if (member.getBalance().compareTo(order.getActualAmount()) < 0) {
                throw new BusinessException("会员余额不足");
            }
            member.setBalance(member.getBalance().subtract(order.getActualAmount()));
            // 累计消费和积分（1元=1积分）
            member.setTotalConsumption(member.getTotalConsumption().add(order.getActualAmount()));
            member.setPoints(member.getPoints() + order.getActualAmount().intValue());
            memberMapper.updateById(member);
        }

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPaymentNo(generatePaymentNo());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setAmount(order.getActualAmount());
        payment.setStatus(1);
        payment.setCashierId(SecurityUtils.getCurrentUserId());
        paymentMapper.insert(payment);

        // 更新订单状态为已结算
        order.setOrderStatus(3);
        orderMapper.updateById(order);

        // 释放桌台
        if (order.getTableId() != null) {
            DiningTable table = tableMapper.selectById(order.getTableId());
            if (table != null) {
                table.setStatus(0);
                tableMapper.updateById(table);
            }
        }

        // 扣减库存
        deductInventory(order.getId());

        // 非余额支付的会员也累积积分
        if (dto.getPaymentMethod() != 4 && order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                member.setTotalConsumption(member.getTotalConsumption().add(order.getActualAmount()));
                member.setPoints(member.getPoints() + order.getActualAmount().intValue());
                memberMapper.updateById(member);
            }
        }

        return payment;
    }

    @Override
    @Transactional
    public void refund(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException(404, "支付记录不存在");
        if (payment.getStatus() == 2) throw new BusinessException("已退款");
        payment.setStatus(2);
        paymentMapper.updateById(payment);

        // 退回会员余额
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order != null && payment.getPaymentMethod() == 4 && order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                member.setBalance(member.getBalance().add(payment.getAmount()));
                memberMapper.updateById(member);
            }
        }
    }

    private void deductInventory(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            List<DishIngredient> dishIngredients = dishIngredientMapper.selectList(
                    new LambdaQueryWrapper<DishIngredient>().eq(DishIngredient::getDishId, item.getDishId()));
            for (DishIngredient di : dishIngredients) {
                Ingredient ingredient = ingredientMapper.selectById(di.getIngredientId());
                if (ingredient != null) {
                    BigDecimal consumption = di.getConsumptionAmount().multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal before = ingredient.getStockQuantity();
                    BigDecimal after = before.subtract(consumption);
                    if (after.compareTo(BigDecimal.ZERO) < 0) after = BigDecimal.ZERO;
                    ingredient.setStockQuantity(after);
                    ingredientMapper.updateById(ingredient);

                    // 记录出库
                    InventoryRecord record = new InventoryRecord();
                    record.setIngredientId(ingredient.getId());
                    record.setType(3);
                    record.setQuantity(consumption);
                    record.setBeforeQuantity(before);
                    record.setAfterQuantity(after);
                    record.setRemark("订单消耗-" + item.getDishName());
                    inventoryRecordMapper.insert(record);
                }
            }
        }
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", (int) (Math.random() * 1000));
    }
}
