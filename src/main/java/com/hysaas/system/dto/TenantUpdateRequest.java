package com.hysaas.system.dto;

import lombok.Data;

@Data
public class TenantUpdateRequest {

    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String remark;
}
