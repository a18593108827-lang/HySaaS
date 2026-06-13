package com.hysaas.payment.dto;

import lombok.Data;

@Data
public class PayCreateResultVO {

    /** MOCK | ALIPAY */
    private String payMode;
    /** mock 模式下的接口地址 */
    private String payUrl;
    /** 支付宝返回的自动提交表单 HTML */
    private String payForm;
}
