package com.hysaas.message.dto;

import lombok.Data;

@Data
public class EmailLogVO {

    private Long id;
    private String code;
    private String recipient;
    private String subject;
    private String status;
    private String errorMsg;
    private Integer retryCount;
    private String createdAt;
}
