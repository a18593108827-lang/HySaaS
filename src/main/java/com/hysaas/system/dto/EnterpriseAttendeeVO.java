package com.hysaas.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseAttendeeVO {

    private Long id;
    private String username;
    private String nickname;
    private String status;
    private LocalDateTime createdAt;
}
