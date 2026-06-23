package com.hysaas.message.dto;

import lombok.Data;

@Data
public class EmailTemplateUpdateRequest {

    private String subject;
    private String content;
}
