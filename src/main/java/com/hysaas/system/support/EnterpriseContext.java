package com.hysaas.system.support;

import cn.dev33.satoken.stp.StpUtil;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnterpriseContext {

    private final SysUserMapper sysUserMapper;

    public Long requireTenantId() {
        Object tenantId = StpUtil.getSession().get("tenantId");
        if (!(tenantId instanceof Long id)) {
            throw new BizException(403, "非企业账号");
        }
        return id;
    }

    public SysUser requireEnterpriseUser() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null || !"ENTERPRISE".equals(user.getUserType())) {
            throw new BizException(403, "非企业账号");
        }
        return user;
    }
}
