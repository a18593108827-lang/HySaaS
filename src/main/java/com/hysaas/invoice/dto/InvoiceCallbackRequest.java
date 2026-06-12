package com.hysaas.invoice.dto;

import lombok.Data;

@Data
public class InvoiceCallbackRequest {

    private Long invoiceId;
    private String fileUrl;
    private String status;
}
