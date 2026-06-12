package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.system.dto.EnterpriseAttendeePayload;
import com.hysaas.system.dto.EnterpriseAttendeeVO;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EnterpriseAttendeeService {

    private static final Set<String> USER_STATUSES = Set.of("ENABLED", "DISABLED");

    private final SysUserMapper sysUserMapper;
    private final EnterpriseContext enterpriseContext;
    private final PasswordEncoder passwordEncoder;

    public PageResult<EnterpriseAttendeeVO> page(String nickname, Integer page, Integer size) {
        Long tenantId = enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getUserType, "ATTENDEE")
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(nickname)) {
            wrapper.and(w -> w.like(SysUser::getNickname, nickname).or().like(SysUser::getUsername, nickname));
        }
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        List<EnterpriseAttendeeVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    public EnterpriseAttendeeVO getById(Long id) {
        return toVO(requireAttendee(id));
    }

    @Transactional
    public EnterpriseAttendeeVO create(EnterpriseAttendeePayload payload) {
        Long tenantId = enterpriseContext.requireTenantId();
        if (!StringUtils.hasText(payload.getPassword())) {
            throw new BizException("密码不能为空");
        }
        long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, payload.getUsername()));
        if (exists > 0) {
            throw new BizException("账号已存在");
        }
        String status = StringUtils.hasText(payload.getStatus()) ? payload.getStatus() : "ENABLED";
        if (!USER_STATUSES.contains(status)) {
            throw new BizException("无效的状态");
        }
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUsername(payload.getUsername());
        user.setNickname(payload.getNickname());
        user.setUserType("ATTENDEE");
        user.setStatus(status);
        user.setPassword(passwordEncoder.encode(payload.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        return toVO(user);
    }

    @Transactional
    public EnterpriseAttendeeVO update(Long id, EnterpriseAttendeePayload payload) {
        SysUser user = requireAttendee(id);
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
        return toVO(user);
    }

    @Transactional
    public void delete(Long id) {
        requireAttendee(id);
        sysUserMapper.deleteById(id);
    }

    private SysUser requireAttendee(Long id) {
        Long tenantId = enterpriseContext.requireTenantId();
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || !tenantId.equals(user.getTenantId()) || !"ATTENDEE".equals(user.getUserType())) {
            throw new BizException(404, "参会账号不存在");
        }
        return user;
    }

    private EnterpriseAttendeeVO toVO(SysUser user) {
        EnterpriseAttendeeVO vo = new EnterpriseAttendeeVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
