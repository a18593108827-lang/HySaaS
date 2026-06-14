package com.hysaas.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.system.dto.AttendeeRegisterRequest;
import com.hysaas.system.dto.LoginResultVO;
import com.hysaas.system.entity.SysTenant;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysTenantMapper;
import com.hysaas.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublicAttendeeRegisterService {

    private static final Set<String> OPEN_STATUSES = Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");

    private final EvtEventMapper evtEventMapper;
    private final SysTenantMapper sysTenantMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserContactService userContactService;

    @Transactional
    public LoginResultVO register(AttendeeRegisterRequest request) {
        if (request.getPassword().length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        EvtEvent event = evtEventMapper.selectById(request.getEventId());
        if (event == null || !intToBool(event.getRegistrationEnabled()) || !OPEN_STATUSES.contains(event.getStatus())) {
            throw new BizException("活动未开放报名");
        }
        SysTenant tenant = sysTenantMapper.selectById(event.getTenantId());
        if (tenant == null || !"APPROVED".equals(tenant.getStatus())) {
            throw new BizException("所属企业未通过审核");
        }
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, event.getTenantId())
                .eq(SysUser::getUserType, "ATTENDEE")
                .and(w -> w.eq(SysUser::getEmail, request.getEmail().trim())
                        .or().eq(SysUser::getPhone, request.getPhone().trim()))) > 0) {
            throw new BizException("该邮箱或手机已注册，请直接登录");
        }
        SysUser user = new SysUser();
        user.setTenantId(event.getTenantId());
        userContactService.bindContact(user, request.getEmail(), request.getPhone(), null);
        user.setNickname(request.getNickname().trim());
        user.setUserType("ATTENDEE");
        user.setStatus("ENABLED");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);

        StpUtil.login(user.getId());
        StpUtil.getSession().set("tenantId", user.getTenantId());
        LoginResultVO result = new LoginResultVO();
        result.setToken(StpUtil.getTokenValue());
        result.setUserType(user.getUserType());
        return result;
    }

    private boolean intToBool(Integer value) {
        return value != null && value == 1;
    }
}
