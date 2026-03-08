package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.dto.ReservationDTO;
import com.hotpot.entity.DiningTable;
import com.hotpot.entity.TableReservation;
import com.hotpot.mapper.DiningTableMapper;
import com.hotpot.mapper.TableReservationMapper;
import com.hotpot.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final TableReservationMapper reservationMapper;
    private final DiningTableMapper tableMapper;

    @Override
    public PageResult<TableReservation> list(Integer page, Integer size, Integer status, String keyword) {
        LambdaQueryWrapper<TableReservation> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TableReservation::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TableReservation::getCustomerName, keyword)
                    .or().like(TableReservation::getCustomerPhone, keyword));
        }
        wrapper.orderByDesc(TableReservation::getReservationTime);
        Page<TableReservation> result = reservationMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充桌号
        List<DiningTable> tables = tableMapper.selectList(null);
        Map<Long, String> tableMap = tables.stream()
                .collect(Collectors.toMap(DiningTable::getId, DiningTable::getTableNumber));
        result.getRecords().forEach(r -> r.setTableNumber(tableMap.get(r.getTableId())));

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public TableReservation getById(Long id) {
        TableReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "预订不存在");
        DiningTable table = tableMapper.selectById(reservation.getTableId());
        if (table != null) reservation.setTableNumber(table.getTableNumber());
        return reservation;
    }

    @Override
    @Transactional
    public void create(ReservationDTO dto) {
        DiningTable table = tableMapper.selectById(dto.getTableId());
        if (table == null) throw new BusinessException("桌台不存在");

        TableReservation reservation = new TableReservation();
        BeanUtils.copyProperties(dto, reservation);
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        // 更新桌台状态为预订
        table.setStatus(2);
        tableMapper.updateById(table);
    }

    @Override
    public void update(Long id, ReservationDTO dto) {
        TableReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "预订不存在");
        BeanUtils.copyProperties(dto, reservation);
        reservationMapper.updateById(reservation);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        TableReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "预订不存在");
        reservation.setStatus(2);
        reservationMapper.updateById(reservation);

        // 释放桌台
        DiningTable table = tableMapper.selectById(reservation.getTableId());
        if (table != null && table.getStatus() == 2) {
            table.setStatus(0);
            tableMapper.updateById(table);
        }
    }

    @Override
    @Transactional
    public void arrive(Long id) {
        TableReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "预订不存在");
        reservation.setStatus(1);
        reservationMapper.updateById(reservation);

        // 桌台改为占用
        DiningTable table = tableMapper.selectById(reservation.getTableId());
        if (table != null) {
            table.setStatus(1);
            tableMapper.updateById(table);
        }
    }
}
