package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.payment.dto.PayCreateRequest;
import com.hysaas.payment.dto.PayCreateResultVO;
import com.hysaas.payment.dto.PayMethodsVO;
import com.hysaas.payment.dto.PayOrderVO;
import com.hysaas.payment.service.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalPayController {

    private final PayOrderService payOrderService;

    @GetMapping("/orders")
    @SaCheckRole("ATTENDEE")
    public R<PageResult<PayOrderVO>> orders(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        return R.ok(payOrderService.portalOrders(page, size));
    }

    @GetMapping("/pay/methods")
    @SaCheckRole("ATTENDEE")
    public R<PayMethodsVO> payMethods() {
        return R.ok(payOrderService.payMethods());
    }

    @PostMapping("/pay/create")
    @SaCheckRole("ATTENDEE")
    public R<PayCreateResultVO> createPay(@RequestBody PayCreateRequest request, HttpServletRequest httpRequest) {
        return R.ok(payOrderService.createPay(request, resolveClientIp(httpRequest)));
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return StringUtils.hasText(ip) ? ip : request.getRemoteAddr();
    }

    @PostMapping("/pay/mock/{orderId}")
    @SaCheckRole("ATTENDEE")
    public R<Void> mockPay(@PathVariable Long orderId) {
        payOrderService.mockPay(orderId);
        return R.ok();
    }
}
