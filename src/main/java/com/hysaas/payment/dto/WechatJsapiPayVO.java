package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class WechatJsapiPayVO {

    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageVal;
    private String signType;
    private String paySign;
}
