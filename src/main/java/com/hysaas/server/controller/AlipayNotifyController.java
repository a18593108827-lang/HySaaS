package com.hysaas.server.controller;

import com.hysaas.payment.service.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay/alipay")
@RequiredArgsConstructor
public class AlipayNotifyController {

    private final PayOrderService payOrderService;

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : ""));
        return payOrderService.handleAlipayNotify(params);
    }
}
