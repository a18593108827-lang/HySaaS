package com.hysaas.system.service;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.dto.LoginRequest;
import com.hysaas.system.dto.LoginResultVO;
import com.hysaas.system.dto.UserInfoVO;
import com.hysaas.system.entity.SysEventPermission;
import com.hysaas.system.entity.SysTenant;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysEventPermissionMapper;
import com.hysaas.system.mapper.SysTenantMapper;
import com.hysaas.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysTenantMapper sysTenantMapper;
    private final SysEventPermissionMapper sysEventPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final StpInterface stpInterface;

    public LoginResultVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "账号或密码不正确");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new BizException(403, "账号已禁用");
        }
        validateTenant(user);
        StpUtil.login(user.getId());
        if (user.getTenantId() != null) {
            StpUtil.getSession().set("tenantId", user.getTenantId());
        }
        LoginResultVO result = new LoginResultVO();
        result.setToken(StpUtil.getTokenValue());
        result.setUserType(user.getUserType());
        return result;
    }

    public void logout() {
        StpUtil.logout();
    }

    public UserInfoVO currentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setTenantId(user.getTenantId());
        vo.setRoles(stpInterface.getRoleList(userId, ""));
        List<SysEventPermission> perms = sysEventPermissionMapper.selectList(
                new LambdaQueryWrapper<SysEventPermission>().eq(SysEventPermission::getUserId, userId));
        vo.setEventPermissions(perms.stream().map(SysEventPermission::getPermCode).distinct().toList());
        return vo;
    }

    private void validateTenant(SysUser user) {
        if (user.getTenantId() == null) {
            return;
        }
        SysTenant tenant = sysTenantMapper.selectById(user.getTenantId());
        if (tenant == null || !"APPROVED".equals(tenant.getStatus())) {
            throw new BizException(403, "所属企业未通过审核");
        }
    }
}
