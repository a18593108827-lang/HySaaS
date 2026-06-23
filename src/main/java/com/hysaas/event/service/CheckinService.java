package com.hysaas.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.framework.websocket.CheckinWebSocketHandler;
import com.hysaas.event.dto.CheckinListVO;
import com.hysaas.event.dto.CheckinQrcodeResultVO;
import com.hysaas.event.dto.CheckinRecordVO;
import com.hysaas.event.dto.PortalCheckinRequest;
import com.hysaas.event.entity.EvtCheckin;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.entity.EvtRegistration;
import com.hysaas.event.mapper.EvtCheckinMapper;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.event.mapper.EvtRegistrationMapper;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private static final String SOURCE_SCAN = "SCAN";
    private static final String SOURCE_PROXY = "PROXY";

    private final EvtCheckinMapper evtCheckinMapper;
    private final EvtEventMapper evtEventMapper;
    private final EvtRegistrationMapper evtRegistrationMapper;
    private final RegistrationService registrationService;
    private final EnterpriseContext enterpriseContext;
    private final PortalContext portalContext;
    private final CheckinWebSocketHandler checkinWebSocketHandler;

    @Transactional
    public CheckinQrcodeResultVO generateQrcode(Long eventId) {
        EvtEvent event = requireEnterpriseEvent(eventId);
        String token = UUID.randomUUID().toString().replace("-", "");
        event.setCheckinToken(token);
        event.setUpdatedAt(LocalDateTime.now());
        evtEventMapper.updateById(event);
        CheckinQrcodeResultVO vo = new CheckinQrcodeResultVO();
        vo.setToken(token);
        return vo;
    }

    public CheckinListVO enterpriseList(Long eventId) {
        requireEnterpriseEvent(eventId);
        long total = registrationService.countApprovedByEvent(eventId);
        List<EvtCheckin> checkins = evtCheckinMapper.selectList(new LambdaQueryWrapper<EvtCheckin>()
                .eq(EvtCheckin::getEventId, eventId)
                .orderByDesc(EvtCheckin::getCheckinAt));
        Map<Long, String> nameMap = loadRegistrationNames(eventId, checkins);
        List<CheckinRecordVO> records = checkins.stream().map(c -> {
            CheckinRecordVO vo = new CheckinRecordVO();
            vo.setUserId(c.getUserId());
            vo.setName(nameMap.getOrDefault(c.getUserId(), "未知"));
            vo.setSource(c.getSource());
            vo.setCheckinTime(formatTime(c.getCheckinAt()));
            return vo;
        }).toList();
        CheckinListVO vo = new CheckinListVO();
        vo.setCount(checkins.size());
        vo.setTotal((int) total);
        vo.setRecords(records);
        return vo;
    }

    @Transactional
    public void portalCheckin(Long eventId, PortalCheckinRequest request) {
        SysUser user = portalContext.requireAttendee();
        EvtEvent event = requirePortalEvent(eventId);
        if (request == null || !StringUtils.hasText(request.getToken())) {
            throw new BizException("请扫描会场签到二维码");
        }
        validateCheckinToken(event, request.getToken());
        registrationService.requireApprovedRegistration(eventId, user.getId());
        insertCheckinIfAbsent(event, user.getId(), SOURCE_SCAN, false);
    }

    @Transactional
    public void enterpriseProxyCheckin(Long eventId, Long registrationId) {
        EvtEvent event = requireEnterpriseEvent(eventId);
        EvtRegistration registration = evtRegistrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new BizException(404, "报名记录不存在");
        }
        if (!eventId.equals(registration.getEventId())) {
            throw new BizException(404, "报名记录不存在");
        }
        if (!"APPROVED".equals(registration.getStatus())) {
            throw new BizException("仅已通过报名可代签");
        }
        if (registration.getUserId() == null) {
            throw new BizException("报名记录缺少参会账号");
        }
        insertCheckinIfAbsent(event, registration.getUserId(), SOURCE_PROXY, true);
    }

    private void insertCheckinIfAbsent(EvtEvent event, Long userId, String source, boolean failIfExists) {
        long exists = evtCheckinMapper.selectCount(new LambdaQueryWrapper<EvtCheckin>()
                .eq(EvtCheckin::getEventId, event.getId())
                .eq(EvtCheckin::getUserId, userId));
        if (exists > 0) {
            if (failIfExists) {
                throw new BizException("该参会人已签到");
            }
            return;
        }
        EvtCheckin checkin = new EvtCheckin();
        checkin.setTenantId(event.getTenantId());
        checkin.setEventId(event.getId());
        checkin.setUserId(userId);
        checkin.setSource(source);
        checkin.setCheckinAt(LocalDateTime.now());
        evtCheckinMapper.insert(checkin);
        pushCheckinStats(event.getId());
    }

    private void pushCheckinStats(Long eventId) {
        long total = registrationService.countApprovedByEvent(eventId);
        long count = evtCheckinMapper.selectCount(new LambdaQueryWrapper<EvtCheckin>()
                .eq(EvtCheckin::getEventId, eventId));
        checkinWebSocketHandler.broadcast(eventId, (int) count, (int) total);
    }

    private Map<Long, String> loadRegistrationNames(Long eventId, List<EvtCheckin> checkins) {
        List<Long> userIds = checkins.stream().map(EvtCheckin::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return evtRegistrationMapper.selectList(new LambdaQueryWrapper<EvtRegistration>()
                        .eq(EvtRegistration::getEventId, eventId)
                        .in(EvtRegistration::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(EvtRegistration::getUserId, EvtRegistration::getName, (a, b) -> a));
    }

    private void validateCheckinToken(EvtEvent event, String token) {
        if (!StringUtils.hasText(event.getCheckinToken()) || !event.getCheckinToken().equals(token)) {
            throw new BizException("签到码无效或已过期，请重新扫描会场二维码");
        }
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
        if (event == null) {
            throw new BizException(404, "活动不存在");
        }
        return event;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
