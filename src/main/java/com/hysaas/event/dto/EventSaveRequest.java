package com.hysaas.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventSaveRequest {

    @NotBlank(message = "活动名称不能为空")
    private String title;
    private String location;
    private String startTime;
    private String endTime;
    private Boolean registrationEnabled;
    private Boolean paperEnabled;
    private Boolean hotelEnabled;
}
