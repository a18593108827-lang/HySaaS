package com.hysaas.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
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

    private final EvtCheckinMapper evtCheckinMapper;
    private final EvtEventMapper evtEventMapper;
    private final EvtRegistrationMapper evtRegistrationMapper;
    private final RegistrationService registrationService;
    private final EnterpriseContext enterpriseContext;
    private final PortalContext portalContext;

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
            vo.setName(nameMap.getOrDefault(c.getUserId(), "未知"));
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
        if (StringUtils.hasText(request.getToken())) {
            validateCheckinToken(event, request.getToken());
        }
        registrationService.requireApprovedRegistration(eventId, user.getId());
        long exists = evtCheckinMapper.selectCount(new LambdaQueryWrapper<EvtCheckin>()
                .eq(EvtCheckin::getEventId, eventId)
                .eq(EvtCheckin::getUserId, user.getId()));
        if (exists > 0) {
            return;
        }
        EvtCheckin checkin = new EvtCheckin();
        checkin.setTenantId(event.getTenantId());
        checkin.setEventId(eventId);
        checkin.setUserId(user.getId());
        checkin.setCheckinAt(LocalDateTime.now());
        evtCheckinMapper.insert(checkin);
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
            throw new BizException("签到码无效");
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
