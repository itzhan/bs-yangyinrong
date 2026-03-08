package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.dto.DishDTO;
import com.hotpot.entity.Dish;
import com.hotpot.entity.DishCategory;
import com.hotpot.mapper.DishCategoryMapper;
import com.hotpot.mapper.DishMapper;
import com.hotpot.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;

    @Override
    public PageResult<Dish> list(Integer page, Integer size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Dish::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Dish::getStatus, status);
        }
        wrapper.orderByAsc(Dish::getSortOrder).orderByDesc(Dish::getCreatedAt);
        Page<Dish> result = dishMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充分类名称
        List<DishCategory> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(DishCategory::getId, DishCategory::getName));
        result.getRecords().forEach(dish -> dish.setCategoryName(categoryMap.get(dish.getCategoryId())));

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Dish getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) throw new BusinessException(404, "菜品不存在");
        DishCategory category = categoryMapper.selectById(dish.getCategoryId());
        if (category != null) dish.setCategoryName(category.getName());
        return dish;
    }

    @Override
    public void create(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);
        if (dish.getStatus() == null) dish.setStatus(1);
        if (dish.getSpicyLevel() == null) dish.setSpicyLevel(0);
        if (dish.getIsRecommended() == null) dish.setIsRecommended(0);
        if (dish.getSortOrder() == null) dish.setSortOrder(0);
        dishMapper.insert(dish);
    }

    @Override
    public void update(Long id, DishDTO dto) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) throw new BusinessException(404, "菜品不存在");
        BeanUtils.copyProperties(dto, dish);
        dishMapper.updateById(dish);
    }

    @Override
    public void delete(Long id) {
        dishMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) throw new BusinessException(404, "菜品不存在");
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }
}
