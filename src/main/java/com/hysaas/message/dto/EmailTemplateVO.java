package com.hysaas.message.dto;

import lombok.Data;

@Data
public class EmailTemplateVO {

    private Long id;
    private String code;
    private String name;
    private String subject;
    private String content;
    private Long eventId;
}
