package com.hysaas.event.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PortalRegisterRequest {

    private String name;
    private String email;
    private String phone;
    private String memberType;
    private String org;
    private String inviteToken;
}
