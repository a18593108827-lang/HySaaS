package com.hysaas.invoice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceVO {

    private Long id;
    private String orderNo;
    private String title;
    private BigDecimal amount;
    private String status;
    private String fileUrl;
    private String createdAt;
}
