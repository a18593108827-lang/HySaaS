package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.event.dto.PortalCheckinRequest;
import com.hysaas.event.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/checkin")
@RequiredArgsConstructor
@SaCheckRole("ATTENDEE")
public class PortalCheckinController {

    private final CheckinService checkinService;

    @PostMapping("/{eventId}")
    public R<Void> checkin(@PathVariable Long eventId, @RequestBody(required = false) PortalCheckinRequest request) {
        checkinService.portalCheckin(eventId, request == null ? new PortalCheckinRequest() : request);
        return R.ok();
    }
}
