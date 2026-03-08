package com.hotpot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.entity.*;
import com.hotpot.mapper.*;
import com.hotpot.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberPortalController {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper levelMapper;
    private final MemberRechargeMapper rechargeMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final TableReservationMapper reservationMapper;
    private final DiningTableMapper diningTableMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /** 会员登录（手机号+密码） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) throw new BusinessException("会员不存在");
        // 简单密码验证：会员密码就是手机号后6位
        String expectedPwd = phone.length() >= 6 ? phone.substring(phone.length() - 6) : phone;
        if (!expectedPwd.equals(password)) throw new BusinessException("密码错误");

        String token = jwtUtils.generateToken(member.getId(), member.getPhone(), "MEMBER");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("memberId", member.getId());
        result.put("name", member.getName());
        result.put("phone", member.getPhone());
        return Result.success(result);
    }

    /** 会员注册 */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String phone = body.get("phone");
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (count > 0) throw new BusinessException("手机号已注册");

        Member member = new Member();
        member.setName(name);
        member.setPhone(phone);
        member.setMemberNo("M" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        member.setLevelId(1L);
        member.setBalance(BigDecimal.ZERO);
        member.setPoints(0);
        member.setTotalConsumption(BigDecimal.ZERO);
        member.setStatus(1);
        if (body.get("gender") != null) member.setGender(Integer.valueOf(body.get("gender")));
        memberMapper.insert(member);
        return Result.success();
    }

    /** 获取会员信息 */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestHeader("Authorization") String auth) {
        Long memberId = getMemberIdFromToken(auth);
        Member member = memberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "会员不存在");
        MemberLevel level = levelMapper.selectById(member.getLevelId());

        Map<String, Object> info = new HashMap<>();
        info.put("id", member.getId());
        info.put("memberNo", member.getMemberNo());
        info.put("name", member.getName());
        info.put("phone", member.getPhone());
        info.put("gender", member.getGender());
        info.put("birthday", member.getBirthday());
        info.put("levelName", level != null ? level.getName() : "普通会员");
        info.put("discount", level != null ? level.getDiscount() : 1.00);
        info.put("balance", member.getBalance());
        info.put("points", member.getPoints());
        info.put("totalConsumption", member.getTotalConsumption());
        return Result.success(info);
    }

    /** 获取消费记录 */
    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> myOrders(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = getMemberIdFromToken(auth);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMemberId, memberId)
                        .orderByDesc(Order::getCreatedAt));
        List<Map<String, Object>> records = result.getRecords().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("orderStatus", order.getOrderStatus());
            map.put("totalAmount", order.getTotalAmount());
            map.put("discountAmount", order.getDiscountAmount());
            map.put("actualAmount", order.getActualAmount());
            map.put("createdAt", order.getCreatedAt());
            // 加载订单项
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            map.put("items", items.stream().map(i -> {
                Map<String, Object> m = new HashMap<>();
                m.put("dishName", i.getDishName());
                m.put("quantity", i.getQuantity());
                m.put("subtotal", i.getSubtotal());
                return m;
            }).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
        return Result.success(PageResult.of(records, result.getTotal(), page, size));
    }

    /** 获取充值记录 */
    @GetMapping("/recharges")
    public Result<PageResult<MemberRecharge>> myRecharges(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = getMemberIdFromToken(auth);
        Page<MemberRecharge> result = rechargeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MemberRecharge>()
                        .eq(MemberRecharge::getMemberId, memberId)
                        .orderByDesc(MemberRecharge::getCreatedAt));
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /** 获取我的预订记录 */
    @GetMapping("/reservations")
    public Result<PageResult<Map<String, Object>>> myReservations(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = getMemberIdFromToken(auth);
        Member member = memberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "会员不存在");

        String memberPhone = normalizePhone(member.getPhone());
        String memberName = safeTrim(member.getName());
        LambdaQueryWrapper<TableReservation> wrapper = new LambdaQueryWrapper<>();
        if (memberPhone != null && !memberPhone.isBlank() && memberName != null && !memberName.isBlank()) {
            wrapper.and(w -> w.eq(TableReservation::getCustomerPhone, memberPhone)
                    .or()
                    .eq(TableReservation::getCustomerName, memberName));
        } else if (memberPhone != null && !memberPhone.isBlank()) {
            wrapper.eq(TableReservation::getCustomerPhone, memberPhone);
        } else if (memberName != null && !memberName.isBlank()) {
            wrapper.eq(TableReservation::getCustomerName, memberName);
        } else {
            return Result.success(PageResult.of(List.of(), 0, page, size));
        }
        wrapper.orderByDesc(TableReservation::getCreatedAt);
        Page<TableReservation> result = reservationMapper.selectPage(new Page<>(page, size), wrapper);

        List<Map<String, Object>> records = result.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("customerName", r.getCustomerName());
            map.put("customerPhone", r.getCustomerPhone());
            map.put("reservationTime", r.getReservationTime());
            map.put("guestCount", r.getGuestCount());
            map.put("status", r.getStatus());
            map.put("remark", r.getRemark());
            map.put("createdAt", r.getCreatedAt());
            // 查桌台号
            DiningTable table = diningTableMapper.selectById(r.getTableId());
            map.put("tableNumber", table != null ? table.getTableNumber() : "-");
            // 状态文字
            String[] statusTexts = {"待确认", "已确认", "已到店", "已完成", "已取消"};
            int s = r.getStatus() != null ? r.getStatus() : 0;
            map.put("statusText", s >= 0 && s < statusTexts.length ? statusTexts[s] : "未知");
            return map;
        }).collect(Collectors.toList());
        return Result.success(PageResult.of(records, result.getTotal(), page, size));
    }

    private Long getMemberIdFromToken(String auth) {
        try {
            String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) throw new BusinessException(401, "无效的会员令牌");
            return userId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[\\s-]", "").trim();
    }

    private String safeTrim(String text) {
        return text == null ? null : text.trim();
    }
}
