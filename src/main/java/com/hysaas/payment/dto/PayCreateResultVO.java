package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayCreateResultVO {

    /** MOCK | ALIPAY | WECHAT */
    private String payMode;
    /** mock 模式下的接口地址 */
    private String payUrl;
    /** 支付宝返回的自动提交表单 HTML */
    private String payForm;
    /** 微信 Native 二维码链接 */
    private String codeUrl;
    /** 微信 JSAPI 调起参数 */
    private WechatJsapiPayVO wechatJsapi;
}
