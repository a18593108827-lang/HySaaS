package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.message.dto.EmailTemplateUpdateRequest;
import com.hysaas.message.dto.EmailTemplateVO;
import com.hysaas.message.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enterprise/email-templates")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseEmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping
    public R<List<EmailTemplateVO>> list() {
        return R.ok(emailTemplateService.list());
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EmailTemplateUpdateRequest request) {
        emailTemplateService.update(id, request.getContent());
        return R.ok();
    }
}
