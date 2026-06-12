package com.hysaas.framework.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.system.entity.SysRole;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.entity.SysUserRole;
import com.hysaas.system.mapper.SysRoleMapper;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = sysUserMapper.selectById(Long.parseLong(loginId.toString()));
        if (user == null) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        roles.add(user.getUserType());
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
            List<SysRole> roleList = sysRoleMapper.selectBatchIds(roleIds);
            roles.addAll(roleList.stream().map(SysRole::getCode).filter(Objects::nonNull).toList());
        }
        return roles.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
