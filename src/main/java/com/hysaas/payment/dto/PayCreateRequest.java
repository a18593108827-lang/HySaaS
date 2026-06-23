package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayCreateRequest {

    private String bizType;
    private Long bizId;
    /** page=电脑网站支付, wap=手机网站支付; wechat: native/h5/jsapi */
    private String channel;
    /** alipay | wechat，默认 alipay */
    private String provider;
    /** 微信 JSAPI 所需 openid */
    private String openid;
}
