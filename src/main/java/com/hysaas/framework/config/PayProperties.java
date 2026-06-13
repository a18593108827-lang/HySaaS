package com.hysaas.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hysaas.pay")
public class PayProperties {

    /** DEV 模拟支付；生产务必 false */
    private boolean mockEnabled = false;

    /** 对外 API 根地址，如 https://api.example.com/api，用于拼接回调地址 */
    private String publicBaseUrl = "";

    /** 正式环境网关，一般无需修改 */
    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
}
