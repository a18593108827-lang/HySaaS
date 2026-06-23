package com.hysaas.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailTestRequest {

    @NotBlank
    private String to;
}
