package com.hysaas.server.controller;

import com.hysaas.common.result.R;
import com.hysaas.system.dto.TenantApplyRequest;
import com.hysaas.system.dto.TenantVO;
import com.hysaas.system.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantService tenantService;

    @PostMapping("/public/tenant/apply")
    public R<TenantVO> apply(@Valid @RequestBody TenantApplyRequest request) {
        return R.ok(tenantService.apply(request));
    }
}
