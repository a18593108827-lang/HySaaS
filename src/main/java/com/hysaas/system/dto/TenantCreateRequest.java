package com.hysaas.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantCreateRequest {

    @NotBlank(message = "企业名称不能为空")
    private String name;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String contactEmail;
    private String address;
    private String remark;
    private String status;
}
