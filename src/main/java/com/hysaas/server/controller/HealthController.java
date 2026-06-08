package com.hysaas.server.controller;

import com.hysaas.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 健康检查，免登录 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public R<Map<String, String>> health() {
        return R.ok(Map.of("status", "UP"));
    }
}
