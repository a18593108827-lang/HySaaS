package com.hysaas.event.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationVO {

    private Long id;
    private Long eventId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String memberType;
    private String status;
    private String source;
    private LocalDateTime createdAt;
}
