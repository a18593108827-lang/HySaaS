package com.hysaas.system.support;

import cn.dev33.satoken.stp.StpUtil;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortalContext {

    private final SysUserMapper sysUserMapper;

    public SysUser requireAttendee() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null || !"ATTENDEE".equals(user.getUserType())) {
            throw new BizException(403, "非参会账号");
        }
        return user;
    }

    public Long requireTenantId() {
        SysUser user = requireAttendee();
        if (user.getTenantId() == null) {
            throw new BizException(403, "参会账号未绑定租户");
        }
        return user.getTenantId();
    }
}
