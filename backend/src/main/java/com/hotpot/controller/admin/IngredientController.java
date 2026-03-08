package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.IngredientDTO;
import com.hotpot.dto.InventoryRecordDTO;
import com.hotpot.entity.Ingredient;
import com.hotpot.entity.InventoryRecord;
import com.hotpot.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public Result<PageResult<Ingredient>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(ingredientService.list(page, size, keyword, category));
    }

    @GetMapping("/{id}")
    public Result<Ingredient> getById(@PathVariable Long id) {
        return Result.success(ingredientService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody IngredientDTO dto) {
        ingredientService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody IngredientDTO dto) {
        ingredientService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return Result.success();
    }

    @GetMapping("/warning")
    public Result<List<Ingredient>> lowStockWarning() {
        return Result.success(ingredientService.lowStockWarning());
    }

    @PostMapping("/operate")
    public Result<Void> operate(@Valid @RequestBody InventoryRecordDTO dto) {
        ingredientService.operate(dto);
        return Result.success();
    }

    @GetMapping("/records")
    public Result<PageResult<InventoryRecord>> recordList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long ingredientId,
            @RequestParam(required = false) Integer type) {
        return Result.success(ingredientService.recordList(page, size, ingredientId, type));
    }
}
