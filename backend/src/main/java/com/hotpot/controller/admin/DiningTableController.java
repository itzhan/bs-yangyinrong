package com.hotpot.controller.admin;

import com.hotpot.common.Result;
import com.hotpot.dto.DiningTableDTO;
import com.hotpot.entity.DiningTable;
import com.hotpot.service.DiningTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService tableService;

    @GetMapping
    public Result<List<DiningTable>> list(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Integer status) {
        return Result.success(tableService.list(area, status));
    }

    @GetMapping("/{id}")
    public Result<DiningTable> getById(@PathVariable Long id) {
        return Result.success(tableService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody DiningTableDTO dto) {
        tableService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DiningTableDTO dto) {
        tableService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        tableService.updateStatus(id, status);
        return Result.success();
    }
}
