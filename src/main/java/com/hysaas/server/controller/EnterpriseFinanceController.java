package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.result.R;
import com.hysaas.payment.dto.PayOrderVO;
import com.hysaas.payment.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enterprise/finance")
@RequiredArgsConstructor
@SaCheckRole("ENTERPRISE")
public class EnterpriseFinanceController {

    private final PayOrderService payOrderService;

    @GetMapping("/orders")
    public R<PageResult<PayOrderVO>> orders(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        return R.ok(payOrderService.enterpriseOrders(page, size));
    }
}
