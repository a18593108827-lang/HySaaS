package com.hysaas.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantVO {

    private Long id;
    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
