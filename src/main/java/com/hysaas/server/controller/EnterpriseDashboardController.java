package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.EnterpriseDashboardStatsVO;
import com.hysaas.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/dashboard")
@RequiredArgsConstructor
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF, EnterpriseRoles.FINANCE, EnterpriseRoles.EXPERT}, mode = SaMode.OR)
public class EnterpriseDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public R<EnterpriseDashboardStatsVO> stats() {
        return R.ok(dashboardService.enterpriseStats());
    }
}
