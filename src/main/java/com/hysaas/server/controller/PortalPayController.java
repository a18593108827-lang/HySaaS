package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.payment.dto.PayCreateRequest;
import com.hysaas.payment.dto.PayCreateResultVO;
import com.hysaas.payment.dto.PayOrderVO;
import com.hysaas.payment.service.PayOrderService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/pay/create")
    @SaCheckRole("ATTENDEE")
    public R<PayCreateResultVO> createPay(@RequestBody PayCreateRequest request) {
        return R.ok(payOrderService.createPay(request));
    }

    @PostMapping("/pay/mock/{orderId}")
    @SaCheckRole("ATTENDEE")
    public R<Void> mockPay(@PathVariable Long orderId) {
        payOrderService.mockPay(orderId);
        return R.ok();
    }
}
