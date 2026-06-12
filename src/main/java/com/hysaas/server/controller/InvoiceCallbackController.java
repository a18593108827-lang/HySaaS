package com.hysaas.server.controller;

import com.hysaas.invoice.dto.InvoiceCallbackRequest;
import com.hysaas.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceCallbackController {

    private final InvoiceService invoiceService;

    @PostMapping("/callback")
    public String callback(@RequestBody InvoiceCallbackRequest request) {
        invoiceService.handleCallback(request);
        return "success";
    }
}
