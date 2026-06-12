package com.hysaas.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventInviteRequest {

    private List<Long> userIds;
    private Boolean autoApprove;
}
