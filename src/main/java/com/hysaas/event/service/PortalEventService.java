package com.hysaas.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.dto.EventItemVO;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PortalEventService {

    private static final Set<String> PORTAL_EVENT_STATUSES = Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");

    private final EvtEventMapper evtEventMapper;
    private final PortalContext portalContext;

    public List<EventItemVO> listForAttendee() {
        Long tenantId = portalContext.requireTenantId();
        List<EvtEvent> events = evtEventMapper.selectList(new LambdaQueryWrapper<EvtEvent>()
                .eq(EvtEvent::getTenantId, tenantId)
                .in(EvtEvent::getStatus, PORTAL_EVENT_STATUSES)
                .orderByDesc(EvtEvent::getStartTime));
        return events.stream().map(this::toVO).toList();
    }

    public EventItemVO getForAttendee(Long id, String token) {
        EvtEvent event = evtEventMapper.selectById(id);
        if (event == null) {
            throw new BizException(404, "活动不存在");
        }
        if (StringUtils.hasText(token)) {
            if (!token.equals(event.getCheckinToken())) {
                throw new BizException("签到码无效");
            }
            return toVO(event);
        }
        portalContext.requireTenantId();
        if (!event.getTenantId().equals(portalContext.requireAttendee().getTenantId())
                || !PORTAL_EVENT_STATUSES.contains(event.getStatus())) {
            throw new BizException(404, "活动不存在或未发布");
        }
        return toVO(event);
    }

    private EventItemVO toVO(EvtEvent event) {
        EventItemVO vo = new EventItemVO();
        BeanUtils.copyProperties(event, vo);
        vo.setRegistrationEnabled(intToBool(event.getRegistrationEnabled()));
        vo.setPaperEnabled(intToBool(event.getPaperEnabled()));
        vo.setHotelEnabled(intToBool(event.getHotelEnabled()));
        return vo;
    }

    private boolean intToBool(Integer value) {
        return value != null && value == 1;
    }
}
