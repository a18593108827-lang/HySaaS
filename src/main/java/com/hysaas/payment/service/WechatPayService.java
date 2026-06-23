package com.hysaas.payment.service;

import com.hysaas.common.exception.BizException;
import com.hysaas.framework.config.PayProperties;
import com.hysaas.payment.dto.WechatJsapiPayVO;
import com.hysaas.payment.entity.PayOrder;
import com.hysaas.system.service.ConfigService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.h5.model.SceneInfo;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    private final ConfigService configService;
    private final PayProperties payProperties;

    private volatile Config cachedConfig;
    private volatile String cachedConfigKey;

    public boolean isConfigured() {
        return StringUtils.hasText(configService.getValue("wechatMchId"))
                && StringUtils.hasText(configService.getValue("wechatAppId"))
                && StringUtils.hasText(configService.getValue("wechatApiV3Key"))
                && StringUtils.hasText(configService.getValue("wechatPrivateKey"))
                && StringUtils.hasText(configService.getValue("wechatSerialNo"));
    }

    public String createNativePay(PayOrder order) {
        ensureConfigured();
        try {
            NativePayService service = new NativePayService.Builder().config(getConfig()).build();
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request = new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            fillNative(request, order);
            return service.prepay(request).getCodeUrl();
        } catch (Exception e) {
            log.error("微信 Native 下单失败 orderNo={}", order.getOrderNo(), e);
            throw new BizException("微信下单失败：" + e.getMessage());
        }
    }

    public String createH5Pay(PayOrder order, String clientIp) {
        ensureConfigured();
        if (!StringUtils.hasText(resolveReturnUrl())) {
            throw new BizException("请配置微信 H5 跳转地址 wechatReturnUrl");
        }
        try {
            H5Service service = new H5Service.Builder().config(getConfig()).build();
            com.wechat.pay.java.service.payments.h5.model.PrepayRequest request = new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
            fillH5(request, order, clientIp);
            return service.prepay(request).getH5Url();
        } catch (Exception e) {
            log.error("微信 H5 下单失败 orderNo={}", order.getOrderNo(), e);
            throw new BizException("微信下单失败：" + e.getMessage());
        }
    }

    public WechatJsapiPayVO createJsapiPay(PayOrder order, String openid) {
        ensureConfigured();
        if (!StringUtils.hasText(openid)) {
            throw new BizException("微信 JSAPI 支付需要 openid");
        }
        try {
            JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(getConfig()).build();
            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request = new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            fillJsapi(request, order, openid);
            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            WechatJsapiPayVO vo = new WechatJsapiPayVO();
            vo.setAppId(response.getAppId());
            vo.setTimeStamp(response.getTimeStamp());
            vo.setNonceStr(response.getNonceStr());
            vo.setPackageVal(response.getPackageVal());
            vo.setSignType(response.getSignType());
            vo.setPaySign(response.getPaySign());
            return vo;
        } catch (Exception e) {
            log.error("微信 JSAPI 下单失败 orderNo={}", order.getOrderNo(), e);
            throw new BizException("微信下单失败：" + e.getMessage());
        }
    }

    public Transaction parseNotify(String body, String serial, String nonce, String signature,
                                   String timestamp, String signatureType) {
        ensureConfigured();
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .signType(StringUtils.hasText(signatureType) ? signatureType : "WECHATPAY2-SHA256-RSA2048")
                .body(body)
                .build();
        NotificationParser parser = new NotificationParser((RSAAutoCertificateConfig) getConfig());
        try {
            return parser.parse(requestParam, Transaction.class);
        } catch (ValidationException e) {
            log.warn("微信回调验签失败", e);
            throw new BizException("微信回调验签失败");
        }
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new BizException("请先在平台配置中填写微信商户号、AppId、APIv3 密钥、商户私钥与证书序列号");
        }
        if (!StringUtils.hasText(resolveNotifyUrl())) {
            throw new BizException("请配置微信异步通知地址或 hysaas.pay.public-base-url");
        }
    }

    private Config getConfig() {
        String key = configService.getValue("wechatMchId") + "|"
                + configService.getValue("wechatSerialNo") + "|"
                + configService.getValue("wechatApiV3Key");
        if (cachedConfig != null && key.equals(cachedConfigKey)) {
            return cachedConfig;
        }
        synchronized (this) {
            if (cachedConfig != null && key.equals(cachedConfigKey)) {
                return cachedConfig;
            }
            cachedConfig = buildConfig();
            cachedConfigKey = key;
            return cachedConfig;
        }
    }

    private RSAAutoCertificateConfig buildConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(configService.getValue("wechatMchId"))
                .privateKey(PemUtil.loadPrivateKeyFromString(normalizeKey(configService.getValue("wechatPrivateKey"))))
                .merchantSerialNumber(configService.getValue("wechatSerialNo"))
                .apiV3Key(configService.getValue("wechatApiV3Key"))
                .build();
    }

    private void fillNative(com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request, PayOrder order) {
        request.setAppid(configService.getValue("wechatAppId"));
        request.setMchid(configService.getValue("wechatMchId"));
        request.setDescription(buildSubject(order));
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(resolveNotifyUrl());
        Amount amount = new Amount();
        amount.setTotal(toFen(order));
        amount.setCurrency("CNY");
        request.setAmount(amount);
    }

    private void fillH5(com.wechat.pay.java.service.payments.h5.model.PrepayRequest request, PayOrder order, String clientIp) {
        request.setAppid(configService.getValue("wechatAppId"));
        request.setMchid(configService.getValue("wechatMchId"));
        request.setDescription(buildSubject(order));
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(resolveNotifyUrl());
        com.wechat.pay.java.service.payments.h5.model.Amount amount = new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(toFen(order));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(StringUtils.hasText(clientIp) ? clientIp : "127.0.0.1");
        H5Info h5Info = new H5Info();
        h5Info.setType("Wap");
        h5Info.setAppUrl(resolveReturnUrl());
        sceneInfo.setH5Info(h5Info);
        request.setSceneInfo(sceneInfo);
    }

    private void fillJsapi(com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request, PayOrder order, String openid) {
        request.setAppid(configService.getValue("wechatAppId"));
        request.setMchid(configService.getValue("wechatMchId"));
        request.setDescription(buildSubject(order));
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(resolveNotifyUrl());
        com.wechat.pay.java.service.payments.jsapi.model.Amount amount = new com.wechat.pay.java.service.payments.jsapi.model.Amount();
        amount.setTotal(toFen(order));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);
    }

    private int toFen(PayOrder order) {
        return order.getAmount().multiply(java.math.BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
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
        String configured = configService.getValue("wechatNotifyUrl");
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        String base = trimSlash(payProperties.getPublicBaseUrl());
        if (!StringUtils.hasText(base)) {
            return null;
        }
        return base + "/pay/wechat/notify";
    }

    private String resolveReturnUrl() {
        String configured = configService.getValue("wechatReturnUrl");
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
                .replaceAll("\\s", "");
    }

    private static String trimSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
