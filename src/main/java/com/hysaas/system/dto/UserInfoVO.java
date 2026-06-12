package com.hysaas.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String userType;
    private Long tenantId;
    private List<String> roles;
    private List<String> eventPermissions;
}
