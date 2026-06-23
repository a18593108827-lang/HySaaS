package com.hysaas.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.framework.config.PayProperties;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.entity.EvtRegistration;
import com.hysaas.hotel.entity.HotelBooking;
import com.hysaas.hotel.service.HotelService;
import com.hysaas.payment.dto.PayCreateRequest;
import com.hysaas.payment.dto.PayCreateResultVO;
import com.hysaas.payment.dto.PayMethodsVO;
import com.hysaas.payment.dto.PayOrderVO;
import com.hysaas.payment.entity.PayOrder;
import com.hysaas.payment.entity.PayTransaction;
import com.hysaas.message.service.EmailService;
import com.hysaas.payment.mapper.PayOrderMapper;
import com.hysaas.payment.mapper.PayTransactionMapper;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.wechat.pay.java.service.payments.model.Transaction;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PayOrderService {

    private static final long PAY_TIMEOUT_MINUTES = 30;

    private final PayOrderMapper payOrderMapper;
    private final PayTransactionMapper payTransactionMapper;
    private final PortalContext portalContext;
    private final EnterpriseContext enterpriseContext;
    private final RedissonClient redissonClient;
    private final HotelService hotelService;
    private final PayProperties payProperties;
    private final AlipayService alipayService;
    private final WechatPayService wechatPayService;
    private final EmailService emailService;
    private final SysUserMapper sysUserMapper;

    public PayOrderService(PayOrderMapper payOrderMapper,
                           PayTransactionMapper payTransactionMapper,
                           PortalContext portalContext,
                           EnterpriseContext enterpriseContext,
                           RedissonClient redissonClient,
                           @Lazy HotelService hotelService,
                           PayProperties payProperties,
                           AlipayService alipayService,
                           WechatPayService wechatPayService,
                           EmailService emailService,
                           SysUserMapper sysUserMapper) {
        this.payOrderMapper = payOrderMapper;
        this.payTransactionMapper = payTransactionMapper;
        this.portalContext = portalContext;
        this.enterpriseContext = enterpriseContext;
        this.redissonClient = redissonClient;
        this.hotelService = hotelService;
        this.payProperties = payProperties;
        this.alipayService = alipayService;
        this.wechatPayService = wechatPayService;
        this.emailService = emailService;
        this.sysUserMapper = sysUserMapper;
    }

    public PageResult<PayOrderVO> portalOrders(Integer page, Integer size) {
        SysUser user = portalContext.requireAttendee();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        Page<PayOrder> result = payOrderMapper.selectPage(new Page<>(pageNum + 1L, pageSize),
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getUserId, user.getId())
                        .orderByDesc(PayOrder::getCreatedAt));
        List<PayOrderVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    public PageResult<PayOrderVO> enterpriseOrders(Integer page, Integer size) {
        Long tenantId = enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        Page<PayOrder> result = payOrderMapper.selectPage(new Page<>(pageNum + 1L, pageSize),
                new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getTenantId, tenantId)
                        .orderByDesc(PayOrder::getCreatedAt));
        List<PayOrderVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    public PayMethodsVO payMethods() {
        PayMethodsVO vo = new PayMethodsVO();
        vo.setMock(payProperties.isMockEnabled());
        vo.setAlipay(!payProperties.isMockEnabled() && alipayService.isConfigured());
        vo.setWechat(!payProperties.isMockEnabled() && wechatPayService.isConfigured());
        return vo;
    }

    public PayCreateResultVO createPay(PayCreateRequest request, String clientIp) {
        SysUser user = portalContext.requireAttendee();
        PayOrder order = payOrderMapper.selectById(request.getBizId());
        if (order == null || !user.getId().equals(order.getUserId())) {
            throw new BizException(404, "订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("订单状态不可支付");
        }
        PayCreateResultVO vo = new PayCreateResultVO();
        if (payProperties.isMockEnabled()) {
            vo.setPayMode("MOCK");
            vo.setPayUrl("/api/portal/pay/mock/" + order.getId());
            return vo;
        }
        String provider = StringUtils.hasText(request.getProvider()) ? request.getProvider().toLowerCase() : "alipay";
        if ("wechat".equals(provider)) {
            return createWechatPay(order, request, clientIp);
        }
        String form = alipayService.createPayForm(order, request.getChannel());
        vo.setPayMode("ALIPAY");
        vo.setPayForm(form);
        return vo;
    }

    private PayCreateResultVO createWechatPay(PayOrder order, PayCreateRequest request, String clientIp) {
        PayCreateResultVO vo = new PayCreateResultVO();
        vo.setPayMode("WECHAT");
        String channel = StringUtils.hasText(request.getChannel()) ? request.getChannel().toLowerCase() : "native";
        switch (channel) {
            case "h5" -> vo.setPayUrl(wechatPayService.createH5Pay(order, clientIp));
            case "jsapi" -> vo.setWechatJsapi(wechatPayService.createJsapiPay(order, request.getOpenid()));
            default -> vo.setCodeUrl(wechatPayService.createNativePay(order));
        }
        return vo;
    }

    public PayOrderVO toOrderVO(PayOrder order) {
        return toVO(order);
    }

    @Transactional
    public PayOrder createRegistrationOrder(SysUser user, EvtEvent event, EvtRegistration registration) {
        PayOrder order = new PayOrder();
        order.setOrderNo(genOrderNo());
        order.setTenantId(event.getTenantId());
        order.setUserId(user.getId());
        order.setBizType("REGISTRATION");
        order.setBizId(registration.getId());
        order.setAmount(event.getRegistrationFee());
        order.setStatus("PENDING");
        order.setInvoiceStatus("NONE");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.insert(order);
        return order;
    }

    @Transactional
    public PayOrder createHotelOrder(SysUser user, HotelBooking booking) {
        PayOrder order = new PayOrder();
        order.setOrderNo(genOrderNo());
        order.setTenantId(booking.getTenantId());
        order.setUserId(user.getId());
        order.setBizType("HOTEL");
        order.setBizId(booking.getId());
        order.setAmount(booking.getAmount());
        order.setStatus("PENDING");
        order.setInvoiceStatus("NONE");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.insert(order);
        return order;
    }

    @Transactional
    public void mockPay(Long orderId) {
        if (!payProperties.isMockEnabled()) {
            throw new BizException("当前环境未启用模拟支付");
        }
        SysUser user = portalContext.requireAttendee();
        PayOrder order = payOrderMapper.selectById(orderId);
        if (order == null || !user.getId().equals(order.getUserId())) {
            throw new BizException(404, "订单不存在");
        }
        markPaid(order, "MOCK-" + orderId, "mock");
    }

    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        if (!alipayService.verifyNotify(params)) {
            log.warn("支付宝回调验签失败 params={}", params);
            return "failure";
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        if (!StringUtils.hasText(outTradeNo)) {
            return "failure";
        }
        RLock lock = redissonClient.getLock("pay:notify:" + outTradeNo);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                return "failure";
            }
            PayOrder order = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                    .eq(PayOrder::getOrderNo, outTradeNo)
                    .last("LIMIT 1"));
            if (order == null) {
                return "failure";
            }
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                markPaid(order, tradeNo, params.toString());
            }
            return "success";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "failure";
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public boolean handleWechatNotify(String body, String serial, String nonce, String signature,
                                      String timestamp, String signatureType) {
        Transaction transaction;
        try {
            transaction = wechatPayService.parseNotify(body, serial, nonce, signature, timestamp, signatureType);
        } catch (BizException e) {
            return false;
        }
        String outTradeNo = transaction.getOutTradeNo();
        if (!StringUtils.hasText(outTradeNo)) {
            return false;
        }
        if (!Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            return true;
        }
        RLock lock = redissonClient.getLock("pay:notify:" + outTradeNo);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                return false;
            }
            PayOrder order = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                    .eq(PayOrder::getOrderNo, outTradeNo)
                    .last("LIMIT 1"));
            if (order == null) {
                return false;
            }
            markPaid(order, transaction.getTransactionId(), body);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public int closeExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAY_TIMEOUT_MINUTES);
        List<PayOrder> orders = payOrderMapper.selectList(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getStatus, "PENDING")
                .lt(PayOrder::getCreatedAt, deadline));
        for (PayOrder order : orders) {
            order.setStatus("CLOSED");
            order.setUpdatedAt(LocalDateTime.now());
            payOrderMapper.updateById(order);
            hotelService.onOrderClosed(order);
        }
        return orders.size();
    }

    private void markPaid(PayOrder order, String tradeNo, String notifyData) {
        if ("PAID".equals(order.getStatus())) {
            return;
        }
        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.updateById(order);
        PayTransaction tx = new PayTransaction();
        tx.setOrderId(order.getId());
        tx.setTradeNo(order.getOrderNo());
        tx.setAlipayTradeNo(tradeNo);
        tx.setStatus("SUCCESS");
        tx.setNotifyData(notifyData);
        tx.setCreatedAt(LocalDateTime.now());
        payTransactionMapper.insert(tx);
        hotelService.onPaySuccess(order);
        sendPaySuccessEmail(order);
    }

    private void sendPaySuccessEmail(PayOrder order) {
        SysUser user = sysUserMapper.selectById(order.getUserId());
        if (user == null) {
            return;
        }
        String email = StringUtils.hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
        if (!StringUtils.hasText(email)) {
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("name", StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        vars.put("orderNo", order.getOrderNo());
        vars.put("amount", order.getAmount() == null ? "" : order.getAmount().stripTrailingZeros().toPlainString());
        vars.put("status", "支付成功");
        emailService.sendTemplate(order.getTenantId(), null, "PAY_SUCCESS", email, vars);
    }

    private PayOrderVO toVO(PayOrder order) {
        PayOrderVO vo = new PayOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private String genOrderNo() {
        return "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 900 + 100);
    }
}
