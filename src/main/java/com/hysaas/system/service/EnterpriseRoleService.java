package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.system.entity.SysRole;
import com.hysaas.system.entity.SysUserRole;
import com.hysaas.system.mapper.SysRoleMapper;
import com.hysaas.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseRoleService {

    private static final Map<String, String> ROLE_NAMES = Map.of(
            "ADMIN", "管理员",
            "EVENT_STAFF", "会务",
            "FINANCE", "财务",
            "EXPERT", "专家"
    );

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public List<String> getRoleCodes(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getCode)
                .distinct()
                .toList();
    }

    public Map<Long, List<String>> getRoleCodesBatch(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Map.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        Map<Long, String> roleCodeMap = sysRoleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getCode));
        Map<Long, List<String>> result = userIds.stream().collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));
        for (SysUserRole userRole : userRoles) {
            String code = roleCodeMap.get(userRole.getRoleId());
            if (code != null) {
                result.computeIfAbsent(userRole.getUserId(), k -> new ArrayList<>()).add(code);
            }
        }
        return result;
    }

    public List<Long> findUserIdsByRole(Long tenantId, String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getCode, roleCode)
                .last("LIMIT 1"));
        if (role == null) {
            return List.of();
        }
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getTenantId, tenantId)
                        .eq(SysUserRole::getRoleId, role.getId()))
                .stream()
                .map(SysUserRole::getUserId)
                .toList();
    }

    @Transactional
    public void replaceRoles(Long userId, Long tenantId, List<String> roleCodes) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId));
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        Set<String> uniqueCodes = new LinkedHashSet<>(roleCodes);
        for (String code : uniqueCodes) {
            Long roleId = getOrCreateRoleId(tenantId, code);
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setTenantId(tenantId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private Long getOrCreateRoleId(Long tenantId, String code) {
        if (!ROLE_NAMES.containsKey(code)) {
            throw new com.hysaas.common.exception.BizException("无效的角色: " + code);
        }
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getCode, code)
                .last("LIMIT 1"));
        if (role != null) {
            return role.getId();
        }
        role = new SysRole();
        role.setTenantId(tenantId);
        role.setCode(code);
        role.setName(ROLE_NAMES.get(code));
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role.getId();
    }
}
