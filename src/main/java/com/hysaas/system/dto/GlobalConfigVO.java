package com.hysaas.system.dto;

import lombok.Data;

@Data
public class GlobalConfigVO {

    private String smtpHost = "";
    private String smtpPort = "465";
    private String smtpUser = "";
    private String smtpPass = "";
    private String alipayAppId = "";
    private String alipayPrivateKey = "";
    private String alipayPublicKey = "";
    private String alipayNotifyUrl = "";
    private String alipayReturnUrl = "";
    private String invoiceAppKey = "";
    private String invoiceAppSecret = "";
}
