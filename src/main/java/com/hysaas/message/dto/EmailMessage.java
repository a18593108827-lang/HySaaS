package com.hysaas.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private Long tenantId;
    private Long eventId;
    private String code;
    private String to;
    private Map<String, String> vars;
    private int retryCount;
}
