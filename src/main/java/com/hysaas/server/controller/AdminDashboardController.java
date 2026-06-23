package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.AdminDashboardStatsVO;
import com.hysaas.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@SaCheckRole("PLATFORM")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public R<AdminDashboardStatsVO> stats() {
        return R.ok(dashboardService.adminStats());
    }
}
