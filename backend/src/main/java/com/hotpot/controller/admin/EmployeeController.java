package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.EmployeeDTO;
import com.hotpot.service.EmployeeService;
import com.hotpot.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<PageResult<EmployeeVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return Result.success(employeeService.list(page, size, keyword, role));
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody EmployeeDTO dto) {
        employeeService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        employeeService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        employeeService.resetPassword(id, body.get("password"));
        return Result.success();
    }
}
