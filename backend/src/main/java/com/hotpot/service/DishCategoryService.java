package com.hotpot.service;

import com.hotpot.dto.DishCategoryDTO;
import com.hotpot.entity.DishCategory;
import java.util.List;

public interface DishCategoryService {
    List<DishCategory> list(String keyword);
    DishCategory getById(Long id);
    void create(DishCategoryDTO dto);
    void update(Long id, DishCategoryDTO dto);
    void delete(Long id);
}
