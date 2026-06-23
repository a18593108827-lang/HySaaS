package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.result.R;
import com.hysaas.event.dto.RegistrationAuditRequest;
import com.hysaas.event.dto.RegistrationVO;
import com.hysaas.event.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/registrations")
@RequiredArgsConstructor
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF}, mode = SaMode.OR)
public class EnterpriseRegistrationController {

    private final RegistrationService registrationService;

    @PutMapping("/{id}")
    public R<RegistrationVO> audit(@PathVariable Long id, @Valid @RequestBody RegistrationAuditRequest request) {
        return R.ok(registrationService.audit(id, request.getStatus()));
    }
}
