package com.hysaas.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayOrderVO {

    private Long id;
    private String orderNo;
    private String bizType;
    private BigDecimal amount;
    private String status;
    private String invoiceStatus;
    private LocalDateTime createdAt;
}
