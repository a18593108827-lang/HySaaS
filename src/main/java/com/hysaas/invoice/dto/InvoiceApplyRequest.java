package com.hysaas.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceApplyRequest {

    @NotNull
    private Long orderId;
    @NotBlank
    private String title;
    @NotBlank
    private String taxNo;
    @NotBlank
    private String email;
}
