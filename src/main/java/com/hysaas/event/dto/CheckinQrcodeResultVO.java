package com.hysaas.event.dto;

import lombok.Data;

@Data
public class CheckinQrcodeResultVO {

    private String token;
    private String checkinUrl;
    private String qrcodeUrl;
}
