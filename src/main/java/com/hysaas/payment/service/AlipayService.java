package com.hysaas.payment.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.framework.config.PayProperties;
import com.hysaas.payment.entity.PayOrder;
import com.hysaas.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private static final String PRODUCT_CODE_PAGE = "FAST_INSTANT_TRADE_PAY";
    private static final String PRODUCT_CODE_WAP = "QUICK_WAP_WAY";

    private final ConfigService configService;
    private final PayProperties payProperties;
    private final ObjectMapper objectMapper;

    public boolean isConfigured() {
        return StringUtils.hasText(configService.getValue("alipayAppId"))
                && StringUtils.hasText(configService.getValue("alipayPrivateKey"))
                && StringUtils.hasText(configService.getValue("alipayPublicKey"));
    }

    public String createPayForm(PayOrder order, String channel) {
        if (!isConfigured()) {
            throw new BizException("请先在平台配置中填写支付宝 AppId、应用私钥、支付宝公钥");
        }
        String notifyUrl = resolveNotifyUrl();
        String returnUrl = resolveReturnUrl();
        if (!StringUtils.hasText(notifyUrl)) {
            throw new BizException("请配置支付宝异步通知地址或 hysaas.pay.public-base-url");
        }
        if (!StringUtils.hasText(returnUrl)) {
            throw new BizException("请配置支付宝同步跳转地址 alipayReturnUrl");
        }
        try {
            AlipayClient client = buildClient();
            String bizContent = buildBizContent(order, "wap".equalsIgnoreCase(channel));
            if ("wap".equalsIgnoreCase(channel)) {
                AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
                request.setNotifyUrl(notifyUrl);
                request.setReturnUrl(returnUrl);
                request.setBizContent(bizContent);
                return client.pageExecute(request).getBody();
            }
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);
            request.setBizContent(bizContent);
            return client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败 orderNo={}", order.getOrderNo(), e);
            throw new BizException("支付宝下单失败：" + e.getErrMsg());
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }
        String publicKey = normalizeKey(configService.getValue("alipayPublicKey"));
        if (!StringUtils.hasText(publicKey)) {
            log.warn("支付宝公钥未配置，拒绝回调");
            return false;
        }
        try {
            return AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            log.warn("支付宝回调验签失败", e);
            return false;
        }
    }

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                payProperties.getGatewayUrl(),
                configService.getValue("alipayAppId"),
                normalizeKey(configService.getValue("alipayPrivateKey")),
                "json",
                "UTF-8",
                normalizeKey(configService.getValue("alipayPublicKey")),
                "RSA2"
        );
    }

    private String buildBizContent(PayOrder order, boolean wap) {
        Map<String, Object> biz = new LinkedHashMap<>();
        biz.put("out_trade_no", order.getOrderNo());
        biz.put("total_amount", order.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        biz.put("subject", buildSubject(order));
        biz.put("product_code", wap ? PRODUCT_CODE_WAP : PRODUCT_CODE_PAGE);
        try {
            return objectMapper.writeValueAsString(biz);
        } catch (JsonProcessingException e) {
            throw new BizException("构建支付参数失败");
        }
    }

    private String buildSubject(PayOrder order) {
        String prefix = switch (order.getBizType()) {
            case "HOTEL" -> "酒店预订";
            case "REGISTRATION" -> "活动报名";
            default -> "订单支付";
        };
        return prefix + "-" + order.getOrderNo();
    }

    private String resolveNotifyUrl() {
        String configured = configService.getValue("alipayNotifyUrl");
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        String base = trimSlash(payProperties.getPublicBaseUrl());
        if (!StringUtils.hasText(base)) {
            return null;
        }
        return base + "/pay/alipay/notify";
    }

    private String resolveReturnUrl() {
        String configured = configService.getValue("alipayReturnUrl");
        return StringUtils.hasText(configured) ? configured.trim() : null;
    }

    private static String normalizeKey(String key) {
        if (!StringUtils.hasText(key)) {
            return key;
        }
        return key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    private static String trimSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
