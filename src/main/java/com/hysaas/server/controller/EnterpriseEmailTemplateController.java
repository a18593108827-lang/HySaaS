package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.hysaas.common.constant.EnterpriseRoles;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.message.dto.EmailLogVO;
import com.hysaas.message.dto.EmailTemplateUpdateRequest;
import com.hysaas.message.dto.EmailTemplateVO;
import com.hysaas.message.service.EmailLogService;
import com.hysaas.message.service.EmailTemplateService;
import com.hysaas.system.support.EnterpriseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enterprise/email-templates")
@RequiredArgsConstructor
@SaCheckRole(value = {EnterpriseRoles.ADMIN, EnterpriseRoles.EVENT_STAFF}, mode = SaMode.OR)
public class EnterpriseEmailTemplateController {

    private final EmailTemplateService emailTemplateService;
    private final EmailLogService emailLogService;
    private final EnterpriseContext enterpriseContext;

    @GetMapping
    public R<List<EmailTemplateVO>> list(@RequestParam(required = false) Long eventId) {
        return R.ok(emailTemplateService.list(eventId));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EmailTemplateUpdateRequest request) {
        emailTemplateService.update(id, request.getSubject(), request.getContent());
        return R.ok();
    }

    @GetMapping("/logs")
    public R<PageResult<EmailLogVO>> logs(@RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size) {
        Long tenantId = enterpriseContext.requireTenantId();
        return R.ok(emailLogService.pageByTenant(tenantId, page, size));
    }
}
