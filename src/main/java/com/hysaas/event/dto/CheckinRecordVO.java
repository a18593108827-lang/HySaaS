package com.hysaas.event.dto;

import lombok.Data;

@Data
public class CheckinRecordVO {

    private Long userId;
    private String name;
    private String source;
    private String checkinTime;
}
