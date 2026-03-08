package com.hotpot.controller;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.InventoryRecordDTO;
import com.hotpot.entity.Ingredient;
import com.hotpot.entity.InventoryRecord;
import com.hotpot.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IngredientService ingredientService;

    @GetMapping("/ingredients")
    public Result<PageResult<Ingredient>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(ingredientService.list(page, size, keyword, category));
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
    public Result<PageResult<InventoryRecord>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long ingredientId,
            @RequestParam(required = false) Integer type) {
        return Result.success(ingredientService.recordList(page, size, ingredientId, type));
    }
}
