package com.hysaas.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String userType;
    private Long tenantId;
    private String tenantName;
    private String status;
    private LocalDateTime createdAt;
}
