package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.dto.AdminUserCreateRequest;
import com.hysaas.system.dto.AdminUserUpdateRequest;
import com.hysaas.system.dto.AdminUserVO;
import com.hysaas.system.entity.SysTenant;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysTenantMapper;
import com.hysaas.system.mapper.SysUserMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Set<String> USER_TYPES = Set.of("PLATFORM", "ENTERPRISE", "ATTENDEE");
    private static final Set<String> USER_STATUSES = Set.of("ENABLED", "DISABLED");

    private final SysUserMapper sysUserMapper;
    private final SysTenantMapper sysTenantMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<AdminUserVO> page(String userType, Long tenantId, Integer page, Integer size) {
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(userType)) {
            wrapper.eq(SysUser::getUserType, userType);
        }
        if (tenantId != null) {
            wrapper.eq(SysUser::getTenantId, tenantId);
        }
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        Map<Long, String> tenantNames = loadTenantNames(result.getRecords());
        List<AdminUserVO> records = result.getRecords().stream()
                .map(user -> toVO(user, tenantNames.get(user.getTenantId())))
                .toList();
        return new PageResult<>(records, result.getTotal());
    }

    public AdminUserVO getById(Long id) {
        SysUser user = requireUser(id);
        String tenantName = resolveTenantName(user.getTenantId());
        return toVO(user, tenantName);
    }

    @Transactional
    public AdminUserVO create(AdminUserCreateRequest request) {
        if (!USER_TYPES.contains(request.getUserType())) {
            throw new BizException("无效的用户类型");
        }
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED";
        if (!USER_STATUSES.contains(status)) {
            throw new BizException("无效的用户状态");
        }
        validateTenantBinding(request.getUserType(), request.getTenantId());
        long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists > 0) {
            throw new BizException("账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setUserType(request.getUserType());
        user.setTenantId(resolveTenantId(request.getUserType(), request.getTenantId()));
        user.setStatus(status);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        return toVO(user, resolveTenantName(user.getTenantId()));
    }

    @Transactional
    public AdminUserVO update(Long id, AdminUserUpdateRequest request) {
        SysUser user = requireUser(id);
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!USER_STATUSES.contains(request.getStatus())) {
                throw new BizException("无效的用户状态");
            }
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toVO(user, resolveTenantName(user.getTenantId()));
    }

    @Transactional
    public void delete(Long id) {
        requireUser(id);
        sysUserMapper.deleteById(id);
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void validateTenantBinding(String userType, Long tenantId) {
        if ("ENTERPRISE".equals(userType)) {
            if (tenantId == null) {
                throw new BizException("企业用户必须选择所属租户");
            }
            requireApprovedTenant(tenantId);
            return;
        }
        if ("PLATFORM".equals(userType) && tenantId != null) {
            throw new BizException("平台用户不能绑定租户");
        }
    }

    private Long resolveTenantId(String userType, Long tenantId) {
        if ("PLATFORM".equals(userType)) {
            return null;
        }
        return tenantId;
    }

    private SysTenant requireApprovedTenant(Long tenantId) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BizException("租户不存在");
        }
        if (!"APPROVED".equals(tenant.getStatus())) {
            throw new BizException("只能选择已通过的租户");
        }
        return tenant;
    }

    private Map<Long, String> loadTenantNames(List<SysUser> users) {
        List<Long> tenantIds = users.stream()
                .map(SysUser::getTenantId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (tenantIds.isEmpty()) {
            return Map.of();
        }
        return sysTenantMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(SysTenant::getId, SysTenant::getName));
    }

    private String resolveTenantName(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        return tenant == null ? null : tenant.getName();
    }

    private AdminUserVO toVO(SysUser user, String tenantName) {
        AdminUserVO vo = new AdminUserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setTenantName(tenantName);
        return vo;
    }
}
