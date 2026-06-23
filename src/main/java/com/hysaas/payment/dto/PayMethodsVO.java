package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayMethodsVO {

    private boolean mock;
    private boolean alipay;
    private boolean wechat;
}
