package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.TenantAuditRequest;
import com.hysaas.system.dto.TenantCreateRequest;
import com.hysaas.system.dto.TenantUpdateRequest;
import com.hysaas.system.dto.TenantVO;
import com.hysaas.system.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenants")
@RequiredArgsConstructor
@SaCheckRole("PLATFORM")
public class AdminTenantController {

    private final TenantService tenantService;

    @GetMapping
    public R<PageResult<TenantVO>> list(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size) {
        return R.ok(tenantService.page(status, page, size));
    }

    @GetMapping("/{id}")
    public R<TenantVO> detail(@PathVariable Long id) {
        return R.ok(tenantService.getById(id));
    }

    @PostMapping
    public R<TenantVO> create(@Valid @RequestBody TenantCreateRequest request) {
        return R.ok(tenantService.create(request));
    }

    @PutMapping("/{id}")
    public R<TenantVO> update(@PathVariable Long id, @RequestBody TenantUpdateRequest request) {
        return R.ok(tenantService.update(id, request));
    }

    @PutMapping("/{id}/audit")
    public R<TenantVO> audit(@PathVariable Long id, @Valid @RequestBody TenantAuditRequest request) {
        return R.ok(tenantService.audit(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return R.ok();
    }
}
