package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayCreateRequest {

    private String bizType;
    private Long bizId;
    /** page=电脑网站支付, wap=手机网站支付 */
    private String channel;
}
