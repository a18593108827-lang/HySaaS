package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayCreateRequest {

    private String bizType;
    private Long bizId;
}
