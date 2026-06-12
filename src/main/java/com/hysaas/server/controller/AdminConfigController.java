package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.system.dto.GlobalConfigVO;
import com.hysaas.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@SaCheckRole("PLATFORM")
public class AdminConfigController {

    private final ConfigService configService;

    @GetMapping
    public R<GlobalConfigVO> get() {
        return R.ok(configService.getGlobalConfig());
    }

    @PutMapping
    public R<GlobalConfigVO> update(@RequestBody Map<String, String> payload) {
        return R.ok(configService.updateGlobalConfig(payload));
    }
}
