package com.hysaas.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendeeRegisterRequest {

    @NotNull(message = "活动不存在")
    private Long eventId;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "手机不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
