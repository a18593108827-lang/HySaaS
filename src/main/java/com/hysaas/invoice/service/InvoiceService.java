package com.hysaas.invoice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.invoice.dto.InvoiceApplyRequest;
import com.hysaas.invoice.dto.InvoiceCallbackRequest;
import com.hysaas.invoice.dto.InvoiceVO;
import com.hysaas.invoice.entity.InvInvoice;
import com.hysaas.invoice.mapper.InvInvoiceMapper;
import com.hysaas.payment.entity.PayOrder;
import com.hysaas.payment.mapper.PayOrderMapper;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvInvoiceMapper invInvoiceMapper;
    private final PayOrderMapper payOrderMapper;
    private final PortalContext portalContext;
    private final EnterpriseContext enterpriseContext;
    private final InvoiceAsyncService invoiceAsyncService;

    @Transactional
    public void apply(InvoiceApplyRequest request) {
        SysUser user = portalContext.requireAttendee();
        PayOrder order = payOrderMapper.selectById(request.getOrderId());
        if (order == null || !user.getId().equals(order.getUserId())) {
            throw new BizException(404, "订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BizException("仅已支付订单可申请发票");
        }
        if (!"NONE".equals(order.getInvoiceStatus())) {
            throw new BizException("该订单已申请发票");
        }
        long exists = invInvoiceMapper.selectCount(new LambdaQueryWrapper<InvInvoice>()
                .eq(InvInvoice::getOrderId, order.getId()));
        if (exists > 0) {
            throw new BizException("该订单已申请发票");
        }
        InvInvoice invoice = new InvInvoice();
        invoice.setTenantId(order.getTenantId());
        invoice.setUserId(user.getId());
        invoice.setOrderId(order.getId());
        invoice.setTitle(request.getTitle());
        invoice.setTaxNo(request.getTaxNo());
        invoice.setAmount(order.getAmount());
        invoice.setStatus("PENDING");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invInvoiceMapper.insert(invoice);
        order.setInvoiceStatus("APPLYING");
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.updateById(order);
        invoiceAsyncService.mockIssue(invoice.getId(), request.getEmail(), user.getNickname());
    }

    @Transactional
    public void handleCallback(InvoiceCallbackRequest request) {
        if (request.getInvoiceId() == null) {
            throw new BizException("invoiceId 不能为空");
        }
        InvInvoice invoice = invInvoiceMapper.selectById(request.getInvoiceId());
        if (invoice == null) {
            throw new BizException(404, "发票不存在");
        }
        if ("ISSUED".equals(invoice.getStatus())) {
            return;
        }
        invoice.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ISSUED");
        invoice.setFileUrl(request.getFileUrl());
        invoice.setUpdatedAt(LocalDateTime.now());
        invInvoiceMapper.updateById(invoice);
        PayOrder order = payOrderMapper.selectById(invoice.getOrderId());
        if (order != null) {
            order.setInvoiceStatus("ISSUED");
            order.setUpdatedAt(LocalDateTime.now());
            payOrderMapper.updateById(order);
        }
    }

    public List<InvoiceVO> enterpriseList() {
        Long tenantId = enterpriseContext.requireTenantId();
        List<InvInvoice> invoices = invInvoiceMapper.selectList(new LambdaQueryWrapper<InvInvoice>()
                .eq(InvInvoice::getTenantId, tenantId)
                .orderByDesc(InvInvoice::getCreatedAt));
        Map<Long, String> orderNoMap = loadOrderNos(invoices);
        return invoices.stream().map(i -> toVO(i, orderNoMap.get(i.getOrderId()))).toList();
    }

    private Map<Long, String> loadOrderNos(List<InvInvoice> invoices) {
        List<Long> orderIds = invoices.stream().map(InvInvoice::getOrderId).distinct().toList();
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return payOrderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(PayOrder::getId, PayOrder::getOrderNo));
    }

    private InvoiceVO toVO(InvInvoice invoice, String orderNo) {
        InvoiceVO vo = new InvoiceVO();
        vo.setId(invoice.getId());
        vo.setOrderNo(orderNo);
        vo.setTitle(invoice.getTitle());
        vo.setAmount(invoice.getAmount());
        vo.setStatus(invoice.getStatus());
        vo.setFileUrl(invoice.getFileUrl());
        if (invoice.getCreatedAt() != null) {
            vo.setCreatedAt(invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return vo;
    }
}
