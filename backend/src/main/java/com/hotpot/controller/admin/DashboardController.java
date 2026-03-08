package com.hotpot.controller.admin;

import com.hotpot.common.Result;
import com.hotpot.service.DashboardService;
import com.hotpot.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
