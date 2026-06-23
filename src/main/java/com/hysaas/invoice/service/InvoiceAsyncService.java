package com.hysaas.invoice.service;

import com.hysaas.invoice.dto.InvoiceCallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceAsyncService {

    private final InvoiceService invoiceService;

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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("mock invoice interrupted: {}", invoiceId);
        } catch (Exception e) {
            log.error("mock invoice failed: {}", invoiceId, e);
        }
    }
}
