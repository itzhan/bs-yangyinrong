package com.hotpot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.common.BusinessException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    /** 获取所有桌台（旧接口保留兼容） */
    @GetMapping("/tables")
    public Result<List<DiningTable>> availableTables() {
        return Result.success(tableMapper.selectList(
                new LambdaQueryWrapper<DiningTable>()
                        .orderByAsc(DiningTable::getCapacity)
                        .orderByAsc(DiningTable::getTableNumber)));
    }

    /**
     * 查询桌台可用性（按容量分组）
     * @param date 日期 yyyy-MM-dd
     * @param timeSlot 时间段起始小时 如 "18:00"
     */
    @GetMapping("/tables/availability")
    public Result<List<Map<String, Object>>> tableAvailability(
            @RequestParam String date,
            @RequestParam String timeSlot) {

        LocalDate reserveDate = LocalDate.parse(date);
        LocalTime slotStart = LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));
        // 每个时间段为1小时
        LocalDateTime windowStart = LocalDateTime.of(reserveDate, slotStart);
        LocalDateTime windowEnd = windowStart.plusHours(1);

        // 1. 查询所有桌台，按容量分组
        List<DiningTable> allTables = tableMapper.selectList(
                new LambdaQueryWrapper<DiningTable>().orderByAsc(DiningTable::getCapacity));

        Map<Integer, List<DiningTable>> tablesByCapacity = allTables.stream()
                .collect(Collectors.groupingBy(DiningTable::getCapacity, LinkedHashMap::new, Collectors.toList()));

        // 2. 查询该时间窗口内所有有效预订（状态为 待到店0 或 已到店1）
        List<TableReservation> reservations = reservationMapper.selectList(
                new LambdaQueryWrapper<TableReservation>()
                        .in(TableReservation::getStatus, 0, 1)
                        .ge(TableReservation::getReservationTime, windowStart)
                        .lt(TableReservation::getReservationTime, windowEnd));

        Set<Long> reservedTableIds = reservations.stream()
                .map(TableReservation::getTableId)
                .collect(Collectors.toSet());

        // 3. 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<DiningTable>> entry : tablesByCapacity.entrySet()) {
            int capacity = entry.getKey();
            List<DiningTable> tables = entry.getValue();
            int total = tables.size();
            int reserved = (int) tables.stream().filter(t -> reservedTableIds.contains(t.getId())).count();
            int available = total - reserved;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("capacity", capacity);
            item.put("total", total);
            item.put("reserved", reserved);
            item.put("available", available);
            result.add(item);
        }

        return Result.success(result);
    }

    /** 提交预订（无需登录） */
    @PostMapping("/reservations")
    public Result<Void> reserve(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String auth) {

        String customerName = cleanText((String) body.get("customerName"));
        String customerPhone = normalizePhone((String) body.get("customerPhone"));

        // 会员已登录时，仅在未填写时兜底使用会员资料
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

        // 解析预订参数
        int capacity = Integer.parseInt(body.get("capacity").toString());
        String dateStr = (String) body.get("reservationDate");
        String timeSlotStr = (String) body.get("timeSlot");
        int guestCount = Integer.parseInt(body.get("guestCount").toString());

        LocalDate reserveDate = LocalDate.parse(dateStr);
        LocalTime slotStart = LocalTime.parse(timeSlotStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime reservationTime = LocalDateTime.of(reserveDate, slotStart);
        LocalDateTime windowEnd = reservationTime.plusHours(1);

        // 查找该容量的所有桌台
        List<DiningTable> candidateTables = tableMapper.selectList(
                new LambdaQueryWrapper<DiningTable>()
                        .eq(DiningTable::getCapacity, capacity)
                        .orderByAsc(DiningTable::getTableNumber));

        if (candidateTables.isEmpty()) {
            throw new BusinessException(400, "没有" + capacity + "人桌");
        }

        // 查找此时间段已被预订的桌台
        List<TableReservation> existing = reservationMapper.selectList(
                new LambdaQueryWrapper<TableReservation>()
                        .in(TableReservation::getStatus, 0, 1)
                        .ge(TableReservation::getReservationTime, reservationTime)
                        .lt(TableReservation::getReservationTime, windowEnd));

        Set<Long> reservedIds = existing.stream()
                .map(TableReservation::getTableId)
                .collect(Collectors.toSet());

        // 找到一个空闲桌台
        DiningTable freeTable = candidateTables.stream()
                .filter(t -> !reservedIds.contains(t.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(400, "该时间段" + capacity + "人桌已预订满，请选择其他时间或桌型"));

        // 创建预订记录
        TableReservation reservation = new TableReservation();
        reservation.setTableId(freeTable.getId());
        reservation.setMemberId(memberId);
        reservation.setCustomerName(customerName);
        reservation.setCustomerPhone(customerPhone);
        reservation.setReservationTime(reservationTime);
        reservation.setGuestCount(guestCount);
        reservation.setRemark(cleanText((String) body.get("remark")));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

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
}
