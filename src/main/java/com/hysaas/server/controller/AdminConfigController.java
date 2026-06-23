package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.message.dto.EmailLogVO;
import com.hysaas.message.dto.EmailTestRequest;
import com.hysaas.message.service.EmailLogService;
import com.hysaas.message.service.EmailService;
import com.hysaas.system.dto.GlobalConfigVO;
import com.hysaas.system.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@SaCheckRole("PLATFORM")
public class AdminConfigController {

    private final ConfigService configService;
    private final EmailService emailService;
    private final EmailLogService emailLogService;

    @GetMapping
    public R<GlobalConfigVO> get() {
        return R.ok(configService.getGlobalConfig());
    }

    @PutMapping
    public R<GlobalConfigVO> update(@RequestBody Map<String, String> payload) {
        return R.ok(configService.updateGlobalConfig(payload));
    }

    @PostMapping("/test-email")
    public R<Void> testEmail(@Valid @RequestBody EmailTestRequest request) {
        emailService.sendTest(request.getTo().trim());
        return R.ok();
    }

    @GetMapping("/email-logs")
    public R<PageResult<EmailLogVO>> emailLogs(@RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        return R.ok(emailLogService.pageAll(page, size));
    }
}
