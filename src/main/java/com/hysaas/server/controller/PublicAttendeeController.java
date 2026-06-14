package com.hysaas.server.controller;

import com.hysaas.common.result.R;
import com.hysaas.system.dto.AttendeeRegisterRequest;
import com.hysaas.system.dto.LoginResultVO;
import com.hysaas.system.service.PublicAttendeeRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicAttendeeController {

    private final PublicAttendeeRegisterService publicAttendeeRegisterService;

    @PostMapping("/public/attendee/register")
    public R<LoginResultVO> register(@Valid @RequestBody AttendeeRegisterRequest request) {
        return R.ok(publicAttendeeRegisterService.register(request));
    }
}
