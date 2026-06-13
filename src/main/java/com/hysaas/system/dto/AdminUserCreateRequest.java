package com.hysaas.system.dto;

import com.hysaas.common.validation.ContactPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = ContactPatterns.EMAIL_REGEX, message = ContactPatterns.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = ContactPatterns.PHONE_REGEX, message = ContactPatterns.PHONE_MESSAGE)
    private String phone;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "用户类型不能为空")
    private String userType;

    private Long tenantId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少 6 位")
    private String password;

    private String status;
}
