package com.hysaas.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.hysaas.common.result.R;
import com.hysaas.event.dto.EventItemVO;
import com.hysaas.event.dto.PortalRegisterRequest;
import com.hysaas.event.dto.RegistrationVO;
import com.hysaas.event.service.PortalEventService;
import com.hysaas.event.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/events")
@RequiredArgsConstructor
public class PortalEventController {

    private final PortalEventService portalEventService;
    private final RegistrationService registrationService;

    @GetMapping
    @SaCheckRole("ATTENDEE")
    public R<List<EventItemVO>> list() {
        return R.ok(portalEventService.listForAttendee());
    }

    @GetMapping("/{id}")
    public R<EventItemVO> detail(@PathVariable Long id, @RequestParam(required = false) String token) {
        return R.ok(portalEventService.getForAttendee(id, token));
    }

    @PostMapping("/{id}/register")
    @SaCheckRole("ATTENDEE")
    public R<RegistrationVO> register(@PathVariable Long id, @RequestBody PortalRegisterRequest request) {
        return R.ok(registrationService.portalRegister(id, request));
    }
}
