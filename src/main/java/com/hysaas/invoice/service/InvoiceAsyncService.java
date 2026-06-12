package com.hysaas.invoice.service;

import com.hysaas.invoice.dto.InvoiceCallbackRequest;
import com.hysaas.invoice.entity.InvInvoice;
import com.hysaas.invoice.mapper.InvInvoiceMapper;
import com.hysaas.message.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceAsyncService {

    private final InvInvoiceMapper invInvoiceMapper;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    @Async
    public void mockIssue(Long invoiceId, String email, String name) {
        try {
            Thread.sleep(1500);
            String fileUrl = "https://invoice.example.com/download/" + invoiceId;
            InvoiceCallbackRequest callback = new InvoiceCallbackRequest();
            callback.setInvoiceId(invoiceId);
            callback.setFileUrl(fileUrl);
            callback.setStatus("ISSUED");
            invoiceService.handleCallback(callback);
            InvInvoice invoice = invInvoiceMapper.selectById(invoiceId);
            if (invoice != null) {
                Map<String, String> vars = new HashMap<>();
                vars.put("name", name == null ? "" : name);
                vars.put("fileUrl", fileUrl);
                emailService.sendTemplate(invoice.getTenantId(), null, "INVOICE_READY", email, vars);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("mock invoice interrupted: {}", invoiceId);
        } catch (Exception e) {
            log.error("mock invoice failed: {}", invoiceId, e);
        }
    }
}
