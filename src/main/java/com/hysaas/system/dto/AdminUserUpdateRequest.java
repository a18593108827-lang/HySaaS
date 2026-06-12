package com.hysaas.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    private String nickname;
    private String status;

    @Size(min = 6, message = "密码至少 6 位")
    private String password;
}
