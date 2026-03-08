package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.DishDTO;
import com.hotpot.entity.Dish;

public interface DishService {
    PageResult<Dish> list(Integer page, Integer size, String keyword, Long categoryId, Integer status);
    Dish getById(Long id);
    void create(DishDTO dto);
    void update(Long id, DishDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
