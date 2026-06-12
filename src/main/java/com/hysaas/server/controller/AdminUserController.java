package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.AdminUserCreateRequest;
import com.hysaas.system.dto.AdminUserUpdateRequest;
import com.hysaas.system.dto.AdminUserVO;
import com.hysaas.system.service.AdminUserService;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@SaCheckRole("PLATFORM")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public R<PageResult<AdminUserVO>> list(@RequestParam(required = false) String userType,
                                           @RequestParam(required = false) Long tenantId,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return R.ok(adminUserService.page(userType, tenantId, page, size));
    }

    @GetMapping("/{id}")
    public R<AdminUserVO> detail(@PathVariable Long id) {
        return R.ok(adminUserService.getById(id));
    }

    @PostMapping
    public R<AdminUserVO> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return R.ok(adminUserService.create(request));
    }

    @PutMapping("/{id}")
    public R<AdminUserVO> update(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return R.ok(adminUserService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return R.ok();
    }
}
