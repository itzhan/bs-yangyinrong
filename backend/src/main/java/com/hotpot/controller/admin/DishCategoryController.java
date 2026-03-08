package com.hotpot.controller.admin;

import com.hotpot.common.Result;
import com.hotpot.dto.DishCategoryDTO;
import com.hotpot.entity.DishCategory;
import com.hotpot.service.DishCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class DishCategoryController {

    private final DishCategoryService categoryService;

    @GetMapping
    public Result<List<DishCategory>> list(@RequestParam(required = false) String keyword) {
        return Result.success(categoryService.list(keyword));
    }

    @GetMapping("/{id}")
    public Result<DishCategory> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody DishCategoryDTO dto) {
        categoryService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DishCategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
