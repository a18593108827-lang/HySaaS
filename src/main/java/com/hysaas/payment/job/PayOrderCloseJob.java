package com.hysaas.payment.job;

import com.hysaas.payment.service.PayOrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayOrderCloseJob {

    private final PayOrderService payOrderService;

    @XxlJob("payOrderCloseJob")
    public void execute() {
        int count = payOrderService.closeExpiredOrders();
        log.info("关闭超时未支付订单 {} 笔", count);
    }
}
