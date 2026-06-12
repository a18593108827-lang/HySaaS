package com.hysaas.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationAuditRequest {

    @NotBlank(message = "审核状态不能为空")
    private String status;
}
