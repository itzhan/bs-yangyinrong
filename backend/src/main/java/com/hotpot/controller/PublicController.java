package com.hotpot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.entity.DiningTable;
import com.hotpot.entity.Dish;
import com.hotpot.entity.DishCategory;
import com.hotpot.entity.Member;
import com.hotpot.entity.TableReservation;
import com.hotpot.mapper.DiningTableMapper;
import com.hotpot.mapper.DishCategoryMapper;
import com.hotpot.mapper.DishMapper;
import com.hotpot.mapper.MemberMapper;
import com.hotpot.mapper.TableReservationMapper;
import com.hotpot.security.JwtUtils;
import com.hotpot.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final DishCategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final DishService dishService;
    private final DiningTableMapper tableMapper;
    private final TableReservationMapper reservationMapper;
    private final MemberMapper memberMapper;
    private final JwtUtils jwtUtils;

    /** 获取所有启用的菜品分类 */
    @GetMapping("/categories")
    public Result<List<DishCategory>> categories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<DishCategory>()
                        .eq(DishCategory::getStatus, 1)
                        .orderByAsc(DishCategory::getSortOrder)));
    }

    /** 获取在售菜品列表（分页） */
    @GetMapping("/dishes")
    public Result<PageResult<Dish>> dishes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(dishService.list(page, size, keyword, categoryId, 1));
    }

    /** 获取菜品详情 */
    @GetMapping("/dishes/{id}")
    public Result<Dish> dishDetail(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    /** 获取可预订的空闲桌台 */
    @GetMapping("/tables")
    public Result<List<DiningTable>> availableTables() {
        return Result.success(tableMapper.selectList(
                new LambdaQueryWrapper<DiningTable>()
                        .eq(DiningTable::getStatus, 0)
                        .orderByAsc(DiningTable::getTableNumber)));
    }

    /** 提交预订（无需登录） */
    @PostMapping("/reservations")
    public Result<Void> reserve(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        TableReservation reservation = new TableReservation();
        String customerName = cleanText((String) body.get("customerName"));
        String customerPhone = normalizePhone((String) body.get("customerPhone"));

        // 会员已登录时，仅在未填写时兜底使用会员资料；不覆盖用户手动填写的联系电话
        Long memberId = extractMemberId(auth);
        if (memberId != null) {
            Member member = memberMapper.selectById(memberId);
            if (member != null) {
                if (customerName == null || customerName.isBlank()) {
                    customerName = cleanText(member.getName());
                }
                if (customerPhone == null || customerPhone.isBlank()) {
                    customerPhone = normalizePhone(member.getPhone());
                }
            }
        }

        reservation.setTableId(Long.valueOf(body.get("tableId").toString()));
        reservation.setCustomerName(customerName);
        reservation.setCustomerPhone(customerPhone);
        reservation.setReservationTime(parseFlexibleDateTime((String) body.get("reservationTime")));
        reservation.setGuestCount(Integer.valueOf(body.get("guestCount").toString()));
        reservation.setRemark(cleanText((String) body.get("remark")));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        // 更新桌台状态为预订
        DiningTable table = tableMapper.selectById(reservation.getTableId());
        if (table != null) {
            table.setStatus(2);
            tableMapper.updateById(table);
        }
        return Result.success();
    }

    private Long extractMemberId(String auth) {
        try {
            if (auth == null || auth.isBlank()) return null;
            String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
            if (!jwtUtils.validateToken(token)) return null;
            String role = jwtUtils.getRoleFromToken(token);
            if (!"MEMBER".equals(role)) return null;
            return jwtUtils.getUserIdFromToken(token);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String cleanText(String text) {
        return text == null ? null : text.trim();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[\\s-]", "").trim();
    }

    /** 兼容多种日期格式：ISO 8601 (带Z/毫秒)、yyyy-MM-ddTHH:mm:ss、yyyy-MM-dd HH:mm:ss */
    private LocalDateTime parseFlexibleDateTime(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("日期不能为空");
        // 处理 UTC 格式 (2026-02-23T10:39:53.000Z)
        if (text.endsWith("Z") || text.contains("+")) {
            return Instant.parse(text).atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        }
        // 处理空格分隔 (2026-02-23 18:00:00)
        if (text.contains(" ") && !text.contains("T")) {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        // 标准格式 (2026-02-23T18:00:00)
        return LocalDateTime.parse(text);
    }
}
