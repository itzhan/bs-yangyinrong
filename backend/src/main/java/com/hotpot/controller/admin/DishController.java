package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.DishDTO;
import com.hotpot.entity.Dish;
import com.hotpot.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping
    public Result<PageResult<Dish>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(dishService.list(page, size, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody DishDTO dto) {
        dishService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DishDTO dto) {
        dishService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        dishService.updateStatus(id, status);
        return Result.success();
    }
}
