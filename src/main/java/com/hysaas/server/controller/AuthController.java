package com.hysaas.server.controller;

import com.hysaas.common.result.R;
import com.hysaas.system.dto.LoginRequest;
import com.hysaas.system.dto.LoginResultVO;
import com.hysaas.system.dto.UserInfoVO;
import com.hysaas.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public R<LoginResultVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/auth/me")
    public R<UserInfoVO> me() {
        return R.ok(authService.currentUser());
    }
}
