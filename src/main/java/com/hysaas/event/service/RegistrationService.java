package com.hysaas.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.dto.EventInviteLinkResultVO;
import com.hysaas.event.dto.EventInviteRequest;
import com.hysaas.event.dto.EventInviteResultVO;
import com.hysaas.event.dto.PortalRegisterRequest;
import com.hysaas.event.dto.RegistrationVO;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.entity.EvtRegistration;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.event.mapper.EvtRegistrationMapper;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Set<String> PORTAL_EVENT_STATUSES = Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");

    private final EvtRegistrationMapper evtRegistrationMapper;
    private final EvtEventMapper evtEventMapper;
    private final SysUserMapper sysUserMapper;
    private final EnterpriseContext enterpriseContext;
    private final PortalContext portalContext;

    public PageResult<RegistrationVO> pageByEvent(Long eventId, String status, Integer page, Integer size) {
        requireEnterpriseEvent(eventId);
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<EvtRegistration> wrapper = new LambdaQueryWrapper<EvtRegistration>()
                .eq(EvtRegistration::getEventId, eventId)
                .orderByDesc(EvtRegistration::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(EvtRegistration::getStatus, status);
        }
        Page<EvtRegistration> result = evtRegistrationMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        List<RegistrationVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    @Transactional
    public RegistrationVO audit(Long id, String status) {
        if (!Set.of("APPROVED", "REJECTED").contains(status)) {
            throw new BizException("审核状态仅支持 APPROVED 或 REJECTED");
        }
        enterpriseContext.requireTenantId();
        EvtRegistration registration = evtRegistrationMapper.selectById(id);
        if (registration == null) {
            throw new BizException(404, "报名记录不存在");
        }
        requireEnterpriseEvent(registration.getEventId());
        if (!"PENDING".equals(registration.getStatus())) {
            throw new BizException("仅待审核报名可审核");
        }
        registration.setStatus(status);
        evtRegistrationMapper.updateById(registration);
        return toVO(registration);
    }

    @Transactional
    public EventInviteResultVO invite(Long eventId, EventInviteRequest request) {
        EvtEvent event = requireEnterpriseEvent(eventId);
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new BizException("请选择参会人");
        }
        boolean autoApprove = Boolean.TRUE.equals(request.getAutoApprove());
        int invited = 0;
        int skipped = 0;
        for (Long userId : request.getUserIds()) {
            SysUser attendee = requireAttendeeUser(userId, event.getTenantId());
            if (existsRegistration(eventId, userId)) {
                skipped++;
                continue;
            }
            EvtRegistration registration = buildRegistration(event, attendee, autoApprove ? "APPROVED" : "PENDING", "INVITE");
            registration.setName(attendee.getNickname());
            registration.setEmail(attendee.getUsername());
            evtRegistrationMapper.insert(registration);
            invited++;
        }
        EventInviteResultVO result = new EventInviteResultVO();
        result.setInvited(invited);
        result.setSkipped(skipped);
        return result;
    }

    @Transactional
    public EventInviteLinkResultVO generateInviteLink(Long eventId) {
        EvtEvent event = requireEnterpriseEvent(eventId);
        String token = "inv-" + eventId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        event.setInviteUrl(token);
        event.setUpdatedAt(LocalDateTime.now());
        evtEventMapper.updateById(event);
        EventInviteLinkResultVO vo = new EventInviteLinkResultVO();
        vo.setToken(token);
        return vo;
    }

    @Transactional
    public RegistrationVO portalRegister(Long eventId, PortalRegisterRequest request) {
        SysUser user = portalContext.requireAttendee();
        EvtEvent event = requirePortalEvent(eventId);
        if (!intToBool(event.getRegistrationEnabled())) {
            throw new BizException("该活动未开放报名");
        }
        if (existsRegistration(eventId, user.getId())) {
            throw new BizException("您已报名该活动");
        }
        if (StringUtils.hasText(request.getInviteToken())) {
            validateInviteToken(event, request.getInviteToken());
        }
        String source = StringUtils.hasText(request.getInviteToken()) ? "INVITE_LINK" : "SELF";
        EvtRegistration registration = buildRegistration(event, user, "PENDING", source);
        registration.setName(StringUtils.hasText(request.getName()) ? request.getName() : user.getNickname());
        registration.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : user.getUsername());
        registration.setPhone(request.getPhone());
        registration.setMemberType(StringUtils.hasText(request.getMemberType()) ? request.getMemberType() : "普通会员");
        evtRegistrationMapper.insert(registration);
        return toVO(registration);
    }

    public EvtRegistration requireApprovedRegistration(Long eventId, Long userId) {
        EvtRegistration registration = evtRegistrationMapper.selectOne(new LambdaQueryWrapper<EvtRegistration>()
                .eq(EvtRegistration::getEventId, eventId)
                .eq(EvtRegistration::getUserId, userId)
                .eq(EvtRegistration::getStatus, "APPROVED")
                .last("LIMIT 1"));
        if (registration == null) {
            throw new BizException("请先完成报名并通过审核");
        }
        return registration;
    }

    public long countApprovedByEvent(Long eventId) {
        return evtRegistrationMapper.selectCount(new LambdaQueryWrapper<EvtRegistration>()
                .eq(EvtRegistration::getEventId, eventId)
                .eq(EvtRegistration::getStatus, "APPROVED"));
    }

    private EvtRegistration buildRegistration(EvtEvent event, SysUser user, String status, String source) {
        EvtRegistration registration = new EvtRegistration();
        registration.setTenantId(event.getTenantId());
        registration.setEventId(event.getId());
        registration.setUserId(user.getId());
        registration.setStatus(status);
        registration.setSource(source);
        registration.setCreatedAt(LocalDateTime.now());
        return registration;
    }

    private boolean existsRegistration(Long eventId, Long userId) {
        return evtRegistrationMapper.selectCount(new LambdaQueryWrapper<EvtRegistration>()
                .eq(EvtRegistration::getEventId, eventId)
                .eq(EvtRegistration::getUserId, userId)) > 0;
    }

    private SysUser requireAttendeeUser(Long userId, Long tenantId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !tenantId.equals(user.getTenantId()) || !"ATTENDEE".equals(user.getUserType())) {
            throw new BizException("参会账号不存在");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new BizException("参会账号已禁用: " + user.getUsername());
        }
        return user;
    }

    private EvtEvent requireEnterpriseEvent(Long eventId) {
        enterpriseContext.requireTenantId();
        EvtEvent event = evtEventMapper.selectById(eventId);
        if (event == null) {
            throw new BizException(404, "活动不存在");
        }
        return event;
    }

    private EvtEvent requirePortalEvent(Long eventId) {
        portalContext.requireTenantId();
        EvtEvent event = evtEventMapper.selectById(eventId);
        if (event == null || !PORTAL_EVENT_STATUSES.contains(event.getStatus())) {
            throw new BizException(404, "活动不存在或未发布");
        }
        return event;
    }

    private void validateInviteToken(EvtEvent event, String inviteToken) {
        if (!StringUtils.hasText(event.getInviteUrl()) || !event.getInviteUrl().equals(inviteToken)) {
            throw new BizException("邀请链接无效或已过期");
        }
    }

    private RegistrationVO toVO(EvtRegistration registration) {
        RegistrationVO vo = new RegistrationVO();
        BeanUtils.copyProperties(registration, vo);
        return vo;
    }

    private boolean intToBool(Integer value) {
        return value != null && value == 1;
    }
}
