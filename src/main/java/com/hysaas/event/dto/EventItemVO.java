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
    private java.math.BigDecimal registrationFee;
    private String inviteUrl;
    private String qrcodeUrl;
    private String checkinToken;
    /** 当前参会人报名状态，未报名为 null */
    private String myRegistrationStatus;
    private String myMemberType;
}
