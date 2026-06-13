package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.dto.EnterpriseMemberPayload;
import com.hysaas.system.dto.EnterpriseMemberVO;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.support.EnterpriseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EnterpriseMemberService {

    private static final Set<String> USER_STATUSES = Set.of("ENABLED", "DISABLED");
    private static final Set<String> ROLE_CODES = Set.of("ADMIN", "EVENT_STAFF", "FINANCE", "EXPERT");

    private final SysUserMapper sysUserMapper;
    private final EnterpriseContext enterpriseContext;
    private final EnterpriseRoleService enterpriseRoleService;
    private final PasswordEncoder passwordEncoder;
    private final UserContactService userContactService;

    public PageResult<EnterpriseMemberVO> page(String role, Integer page, Integer size) {
        Long tenantId = enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getUserType, "ENTERPRISE")
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(role)) {
            List<Long> userIds = enterpriseRoleService.findUserIdsByRole(tenantId, role);
            if (userIds.isEmpty()) {
                return new PageResult<>(List.of(), 0);
            }
            wrapper.in(SysUser::getId, userIds);
        }
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        Map<Long, List<String>> roleMap = enterpriseRoleService.getRoleCodesBatch(
                result.getRecords().stream().map(SysUser::getId).toList());
        List<EnterpriseMemberVO> records = result.getRecords().stream()
                .map(user -> toVO(user, roleMap.getOrDefault(user.getId(), List.of())))
                .toList();
        return new PageResult<>(records, result.getTotal());
    }

    public EnterpriseMemberVO getById(Long id) {
        SysUser user = requireMember(id);
        return toVO(user, enterpriseRoleService.getRoleCodes(user.getId()));
    }

    @Transactional
    public EnterpriseMemberVO create(EnterpriseMemberPayload payload) {
        Long tenantId = enterpriseContext.requireTenantId();
        validateRoles(payload.getRoles());
        if (!StringUtils.hasText(payload.getPassword())) {
            throw new BizException("密码不能为空");
        }
        String status = StringUtils.hasText(payload.getStatus()) ? payload.getStatus() : "ENABLED";
        if (!USER_STATUSES.contains(status)) {
            throw new BizException("无效的状态");
        }
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        userContactService.bindContact(user, payload.getEmail(), payload.getPhone(), null);
        user.setNickname(payload.getNickname());
        user.setUserType("ENTERPRISE");
        user.setStatus(status);
        user.setPassword(passwordEncoder.encode(payload.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        enterpriseRoleService.replaceRoles(user.getId(), tenantId, payload.getRoles());
        return toVO(user, payload.getRoles());
    }

    @Transactional
    public EnterpriseMemberVO update(Long id, EnterpriseMemberPayload payload) {
        Long tenantId = enterpriseContext.requireTenantId();
        SysUser user = requireMember(id);
        userContactService.bindContact(user, payload.getEmail(), payload.getPhone(), user.getId());
        if (StringUtils.hasText(payload.getNickname())) {
            user.setNickname(payload.getNickname());
        }
        if (StringUtils.hasText(payload.getStatus())) {
            if (!USER_STATUSES.contains(payload.getStatus())) {
                throw new BizException("无效的状态");
            }
            user.setStatus(payload.getStatus());
        }
        if (StringUtils.hasText(payload.getPassword())) {
            user.setPassword(passwordEncoder.encode(payload.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        if (payload.getRoles() != null) {
            validateRoles(payload.getRoles());
            enterpriseRoleService.replaceRoles(user.getId(), tenantId, payload.getRoles());
        }
        return toVO(user, enterpriseRoleService.getRoleCodes(user.getId()));
    }

    @Transactional
    public void delete(Long id) {
        requireMember(id);
        sysUserMapper.deleteById(id);
    }

    private SysUser requireMember(Long id) {
        Long tenantId = enterpriseContext.requireTenantId();
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || !tenantId.equals(user.getTenantId()) || !"ENTERPRISE".equals(user.getUserType())) {
            throw new BizException(404, "成员不存在");
        }
        return user;
    }

    private void validateRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new BizException("请选择至少一个角色");
        }
        for (String role : roles) {
            if (!ROLE_CODES.contains(role)) {
                throw new BizException("无效的角色: " + role);
            }
        }
    }

    private EnterpriseMemberVO toVO(SysUser user, List<String> roles) {
        EnterpriseMemberVO vo = new EnterpriseMemberVO();
        BeanUtils.copyProperties(user, vo);
        if (!StringUtils.hasText(vo.getEmail())) {
            vo.setEmail(user.getUsername());
        }
        vo.setRoles(roles);
        return vo;
    }
}
