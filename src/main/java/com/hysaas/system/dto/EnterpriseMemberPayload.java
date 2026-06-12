package com.hysaas.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseMemberPayload {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private List<String> roles;

    @Size(min = 6, message = "密码至少 6 位")
    private String password;

    private String status;
}
