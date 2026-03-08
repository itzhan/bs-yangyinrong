package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.common.SecurityUtils;
import com.hotpot.dto.IngredientDTO;
import com.hotpot.dto.InventoryRecordDTO;
import com.hotpot.entity.Ingredient;
import com.hotpot.entity.InventoryRecord;
import com.hotpot.entity.SysEmployee;
import com.hotpot.mapper.IngredientMapper;
import com.hotpot.mapper.InventoryRecordMapper;
import com.hotpot.mapper.SysEmployeeMapper;
import com.hotpot.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientMapper ingredientMapper;
    private final InventoryRecordMapper recordMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    public PageResult<Ingredient> list(Integer page, Integer size, String keyword, String category) {
        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Ingredient::getName, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Ingredient::getCategory, category);
        }
        wrapper.orderByAsc(Ingredient::getCategory).orderByAsc(Ingredient::getName);
        Page<Ingredient> result = ingredientMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Ingredient getById(Long id) {
        Ingredient ingredient = ingredientMapper.selectById(id);
        if (ingredient == null) throw new BusinessException(404, "食材不存在");
        return ingredient;
    }

    @Override
    public void create(IngredientDTO dto) {
        Ingredient ingredient = new Ingredient();
        BeanUtils.copyProperties(dto, ingredient);
        if (ingredient.getStatus() == null) ingredient.setStatus(1);
        if (ingredient.getStockQuantity() == null) ingredient.setStockQuantity(BigDecimal.ZERO);
        if (ingredient.getWarningQuantity() == null) ingredient.setWarningQuantity(new BigDecimal("10"));
        ingredientMapper.insert(ingredient);
    }

    @Override
    public void update(Long id, IngredientDTO dto) {
        Ingredient ingredient = ingredientMapper.selectById(id);
        if (ingredient == null) throw new BusinessException(404, "食材不存在");
        BeanUtils.copyProperties(dto, ingredient);
        ingredientMapper.updateById(ingredient);
    }

    @Override
    public void delete(Long id) {
        ingredientMapper.deleteById(id);
    }

    @Override
    public List<Ingredient> lowStockWarning() {
        return ingredientMapper.selectList(
                new LambdaQueryWrapper<Ingredient>()
                        .apply("stock_quantity <= warning_quantity")
                        .eq(Ingredient::getStatus, 1)
                        .orderByAsc(Ingredient::getStockQuantity));
    }

    @Override
    @Transactional
    public void operate(InventoryRecordDTO dto) {
        Ingredient ingredient = ingredientMapper.selectById(dto.getIngredientId());
        if (ingredient == null) throw new BusinessException("食材不存在");

        BigDecimal before = ingredient.getStockQuantity();
        BigDecimal after;

        switch (dto.getType()) {
            case 0: // 入库
                after = before.add(dto.getQuantity());
                break;
            case 1: // 出库
                if (before.compareTo(dto.getQuantity()) < 0) throw new BusinessException("库存不足");
                after = before.subtract(dto.getQuantity());
                break;
            case 2: // 盘点（数量即为盘点后的实际库存）
                after = dto.getQuantity();
                break;
            default:
                throw new BusinessException("无效的操作类型");
        }

        ingredient.setStockQuantity(after);
        ingredientMapper.updateById(ingredient);

        InventoryRecord record = new InventoryRecord();
        record.setIngredientId(dto.getIngredientId());
        record.setType(dto.getType());
        record.setQuantity(dto.getQuantity());
        record.setBeforeQuantity(before);
        record.setAfterQuantity(after);
        record.setUnitPrice(dto.getUnitPrice());
        if (dto.getUnitPrice() != null) {
            record.setTotalPrice(dto.getUnitPrice().multiply(dto.getQuantity()));
        }
        record.setRemark(dto.getRemark());
        record.setOperatorId(SecurityUtils.getCurrentUserId());
        recordMapper.insert(record);
    }

    @Override
    public PageResult<InventoryRecord> recordList(Integer page, Integer size, Long ingredientId, Integer type) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        if (ingredientId != null) {
            wrapper.eq(InventoryRecord::getIngredientId, ingredientId);
        }
        if (type != null) {
            wrapper.eq(InventoryRecord::getType, type);
        }
        wrapper.orderByDesc(InventoryRecord::getCreatedAt);
        Page<InventoryRecord> result = recordMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充名称
        List<Ingredient> ingredients = ingredientMapper.selectList(null);
        Map<Long, String> ingredientMap = ingredients.stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));
        List<SysEmployee> employees = employeeMapper.selectList(null);
        Map<Long, String> empMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getRealName));

        result.getRecords().forEach(r -> {
            r.setIngredientName(ingredientMap.get(r.getIngredientId()));
            if (r.getOperatorId() != null) r.setOperatorName(empMap.get(r.getOperatorId()));
        });

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }
}
