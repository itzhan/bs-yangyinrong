package com.hotpot.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.entity.OperationLog;
import com.hotpot.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper logMapper;

    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLog::getOperatorName, keyword)
                    .or().like(OperationLog::getDetail, keyword));
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> result = logMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }
}
