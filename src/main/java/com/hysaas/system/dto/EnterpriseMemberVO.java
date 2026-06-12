package com.hysaas.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterpriseMemberVO {

    private Long id;
    private String username;
    private String nickname;
    private List<String> roles;
    private String status;
    private LocalDateTime createdAt;
}
