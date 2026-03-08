package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotpot.common.BusinessException;
import com.hotpot.dto.DishCategoryDTO;
import com.hotpot.entity.Dish;
import com.hotpot.entity.DishCategory;
import com.hotpot.mapper.DishCategoryMapper;
import com.hotpot.mapper.DishMapper;
import com.hotpot.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @Override
    public List<DishCategory> list(String keyword) {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(DishCategory::getName, keyword);
        }
        wrapper.orderByAsc(DishCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public DishCategory getById(Long id) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null) throw new BusinessException(404, "分类不存在");
        return category;
    }

    @Override
    public void create(DishCategoryDTO dto) {
        DishCategory category = new DishCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSortOrder() == null) category.setSortOrder(0);
        categoryMapper.insert(category);
    }

    @Override
    public void update(Long id, DishCategoryDTO dto) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null) throw new BusinessException(404, "分类不存在");
        BeanUtils.copyProperties(dto, category);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        Long count = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (count > 0) throw new BusinessException("该分类下还有菜品，无法删除");
        categoryMapper.deleteById(id);
    }
}
