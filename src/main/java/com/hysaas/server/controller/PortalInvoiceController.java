package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.invoice.dto.InvoiceApplyRequest;
import com.hysaas.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/invoices")
@RequiredArgsConstructor
@SaCheckRole("ATTENDEE")
public class PortalInvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/apply")
    public R<Void> apply(@Valid @RequestBody InvoiceApplyRequest request) {
        invoiceService.apply(request);
        return R.ok();
    }
}
