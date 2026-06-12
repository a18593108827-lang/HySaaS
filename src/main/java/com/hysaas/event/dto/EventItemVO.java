package com.hysaas.event.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventItemVO {

    private Long id;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean registrationEnabled;
    private Boolean paperEnabled;
    private Boolean hotelEnabled;
    private String inviteUrl;
    private String qrcodeUrl;
    private String checkinToken;
}
