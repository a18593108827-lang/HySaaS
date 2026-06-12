package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.dto.TenantApplyRequest;
import com.hysaas.system.dto.TenantAuditRequest;
import com.hysaas.system.dto.TenantCreateRequest;
import com.hysaas.system.dto.TenantUpdateRequest;
import com.hysaas.system.dto.TenantVO;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TenantService {

    private static final Set<String> TENANT_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");
    private static final String DEFAULT_ENTERPRISE_PASSWORD = "123456";

    private final SysTenantMapper sysTenantMapper;
    private final SysUserMapper sysUserMapper;
    private final EnterpriseRoleService enterpriseRoleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TenantVO apply(TenantApplyRequest request) {
        long pendingCount = sysTenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getContactEmail, request.getContactEmail())
                .eq(SysTenant::getStatus, "PENDING"));
        if (pendingCount > 0) {
            throw new BizException("该邮箱已有待审核申请");
        }
        SysTenant tenant = new SysTenant();
        tenant.setName(request.getName());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setAddress(request.getAddress());
        tenant.setRemark(request.getRemark());
        tenant.setStatus("PENDING");
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        sysTenantMapper.insert(tenant);
        return toVO(tenant);
    }

    public PageResult<TenantVO> page(String status, Integer page, Integer size) {
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<SysTenant>()
                .orderByDesc(SysTenant::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysTenant::getStatus, status);
        }
        Page<SysTenant> result = sysTenantMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        List<TenantVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    public TenantVO getById(Long id) {
        SysTenant tenant = requireTenant(id);
        return toVO(tenant);
    }

    @Transactional
    public TenantVO create(TenantCreateRequest request) {
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : "APPROVED";
        if (!TENANT_STATUSES.contains(status)) {
            throw new BizException("无效的租户状态");
        }
        SysTenant tenant = new SysTenant();
        tenant.setName(request.getName());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setAddress(request.getAddress());
        tenant.setRemark(request.getRemark());
        tenant.setStatus(status);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        sysTenantMapper.insert(tenant);
        if ("APPROVED".equals(status)) {
            ensureEnterpriseAdmin(tenant);
        }
        return toVO(tenant);
    }

    @Transactional
    public TenantVO update(Long id, TenantUpdateRequest request) {
        SysTenant tenant = requireTenant(id);
        if (StringUtils.hasText(request.getName())) {
            tenant.setName(request.getName());
        }
        if (StringUtils.hasText(request.getContactName())) {
            tenant.setContactName(request.getContactName());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }
        if (request.getAddress() != null) {
            tenant.setAddress(request.getAddress());
        }
        if (request.getRemark() != null) {
            tenant.setRemark(request.getRemark());
        }
        tenant.setUpdatedAt(LocalDateTime.now());
        sysTenantMapper.updateById(tenant);
        return toVO(tenant);
    }

    @Transactional
    public TenantVO audit(Long id, TenantAuditRequest request) {
        if (!Set.of("APPROVED", "REJECTED").contains(request.getStatus())) {
            throw new BizException("审核状态仅支持 APPROVED 或 REJECTED");
        }
        SysTenant tenant = requireTenant(id);
        if (!"PENDING".equals(tenant.getStatus())) {
            throw new BizException("仅待审核租户可审核");
        }
        tenant.setStatus(request.getStatus());
        tenant.setUpdatedAt(LocalDateTime.now());
        sysTenantMapper.updateById(tenant);
        if ("APPROVED".equals(request.getStatus())) {
            ensureEnterpriseAdmin(tenant);
        }
        return toVO(tenant);
    }

    @Transactional
    public void delete(Long id) {
        requireTenant(id);
        sysTenantMapper.deleteById(id);
    }

    private SysTenant requireTenant(Long id) {
        SysTenant tenant = sysTenantMapper.selectById(id);
        if (tenant == null) {
            throw new BizException(404, "租户不存在");
        }
        return tenant;
    }

    private void ensureEnterpriseAdmin(SysTenant tenant) {
        if (!StringUtils.hasText(tenant.getContactEmail())) {
            return;
        }
        long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenant.getId())
                .eq(SysUser::getUserType, "ENTERPRISE"));
        if (exists > 0) {
            return;
        }
        long usernameUsed = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, tenant.getContactEmail()));
        if (usernameUsed > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setTenantId(tenant.getId());
        user.setUsername(tenant.getContactEmail());
        user.setNickname(tenant.getContactName());
        user.setUserType("ENTERPRISE");
        user.setStatus("ENABLED");
        user.setPassword(passwordEncoder.encode(DEFAULT_ENTERPRISE_PASSWORD));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        enterpriseRoleService.replaceRoles(user.getId(), tenant.getId(), List.of("ADMIN"));
    }

    private TenantVO toVO(SysTenant tenant) {
        TenantVO vo = new TenantVO();
        BeanUtils.copyProperties(tenant, vo);
        return vo;
    }
}
