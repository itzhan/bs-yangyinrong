package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.common.BusinessException;
import com.hotpot.dto.DiningTableDTO;
import com.hotpot.entity.DiningTable;
import com.hotpot.mapper.DiningTableMapper;
import com.hotpot.service.DiningTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableMapper tableMapper;

    @Override
    public List<DiningTable> list(String area, Integer status) {
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(area)) {
            wrapper.eq(DiningTable::getArea, area);
        }
        if (status != null) {
            wrapper.eq(DiningTable::getStatus, status);
        }
        wrapper.orderByAsc(DiningTable::getTableNumber);
        return tableMapper.selectList(wrapper);
    }

    @Override
    public DiningTable getById(Long id) {
        DiningTable table = tableMapper.selectById(id);
        if (table == null) throw new BusinessException(404, "桌台不存在");
        return table;
    }

    @Override
    public void create(DiningTableDTO dto) {
        Long count = tableMapper.selectCount(
                new LambdaQueryWrapper<DiningTable>().eq(DiningTable::getTableNumber, dto.getTableNumber()));
        if (count > 0) throw new BusinessException("桌号已存在");
        DiningTable table = new DiningTable();
        BeanUtils.copyProperties(dto, table);
        if (table.getStatus() == null) table.setStatus(0);
        tableMapper.insert(table);
    }

    @Override
    public void update(Long id, DiningTableDTO dto) {
        DiningTable table = tableMapper.selectById(id);
        if (table == null) throw new BusinessException(404, "桌台不存在");
        if (!table.getTableNumber().equals(dto.getTableNumber())) {
            Long count = tableMapper.selectCount(
                    new LambdaQueryWrapper<DiningTable>().eq(DiningTable::getTableNumber, dto.getTableNumber()));
            if (count > 0) throw new BusinessException("桌号已存在");
        }
        BeanUtils.copyProperties(dto, table);
        tableMapper.updateById(table);
    }

    @Override
    public void delete(Long id) {
        tableMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        DiningTable table = tableMapper.selectById(id);
        if (table == null) throw new BusinessException(404, "桌台不存在");
        table.setStatus(status);
        tableMapper.updateById(table);
    }
}
