package com.hysaas.server.controller;

import com.hysaas.payment.service.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay/wechat")
@RequiredArgsConstructor
public class WechatNotifyController {

    private final PayOrderService payOrderService;

    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> notify(HttpServletRequest request, @RequestBody String body) {
        try {
            boolean ok = payOrderService.handleWechatNotify(
                    body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Signature-Type"));
            if (ok) {
                return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", "失败"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", "FAIL", "message", "失败"));
    }
}
