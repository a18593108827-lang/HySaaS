package com.hysaas.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.dto.EventItemVO;
import com.hysaas.event.dto.EventSaveRequest;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.hotel.service.HotelService;
import com.hysaas.system.support.EnterpriseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EvtEventMapper evtEventMapper;
    private final EnterpriseContext enterpriseContext;
    private final HotelService hotelService;

    public PageResult<EventItemVO> page(Integer page, Integer size) {
        enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        Page<EvtEvent> result = evtEventMapper.selectPage(new Page<>(pageNum + 1L, pageSize),
                new LambdaQueryWrapper<EvtEvent>().orderByDesc(EvtEvent::getCreatedAt));
        List<EventItemVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    public EventItemVO getById(Long id) {
        return toVO(requireEvent(id));
    }

    @Transactional
    public EventItemVO create(EventSaveRequest request) {
        Long tenantId = enterpriseContext.requireTenantId();
        EvtEvent event = new EvtEvent();
        event.setTenantId(tenantId);
        applySaveRequest(event, request);
        event.setStatus("DRAFT");
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        evtEventMapper.insert(event);
        return toVO(event);
    }

    @Transactional
    public EventItemVO update(Long id, EventSaveRequest request) {
        EvtEvent event = requireEvent(id);
        boolean wasHotelEnabled = intToBool(event.getHotelEnabled());
        applySaveRequest(event, request);
        event.setUpdatedAt(LocalDateTime.now());
        evtEventMapper.updateById(event);
        if (!wasHotelEnabled && intToBool(event.getHotelEnabled())) {
            hotelService.ensureEventQuotas(event.getId());
        }
        return toVO(event);
    }

    @Transactional
    public void delete(Long id) {
        requireEvent(id);
        evtEventMapper.deleteById(id);
    }

    @Transactional
    public EventItemVO publish(Long id) {
        EvtEvent event = requireEvent(id);
        if (!"DRAFT".equals(event.getStatus())) {
            throw new BizException("仅草稿活动可发布");
        }
        event.setStatus("PUBLISHED");
        event.setUpdatedAt(LocalDateTime.now());
        evtEventMapper.updateById(event);
        if (intToBool(event.getHotelEnabled())) {
            hotelService.ensureEventQuotas(event.getId());
        }
        return toVO(event);
    }

    private EvtEvent requireEvent(Long id) {
        enterpriseContext.requireTenantId();
        EvtEvent event = evtEventMapper.selectById(id);
        if (event == null) {
            throw new BizException(404, "活动不存在");
        }
        return event;
    }

    private void applySaveRequest(EvtEvent event, EventSaveRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            event.setTitle(request.getTitle());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(parseDateTime(request.getStartTime()));
        }
        if (request.getEndTime() != null) {
            event.setEndTime(parseDateTime(request.getEndTime()));
        }
        if (request.getRegistrationEnabled() != null) {
            event.setRegistrationEnabled(boolToInt(request.getRegistrationEnabled()));
        } else if (event.getRegistrationEnabled() == null) {
            event.setRegistrationEnabled(0);
        }
        if (request.getPaperEnabled() != null) {
            event.setPaperEnabled(boolToInt(request.getPaperEnabled()));
        } else if (event.getPaperEnabled() == null) {
            event.setPaperEnabled(0);
        }
        if (request.getHotelEnabled() != null) {
            event.setHotelEnabled(boolToInt(request.getHotelEnabled()));
        } else if (event.getHotelEnabled() == null) {
            event.setHotelEnabled(0);
        }
        if (request.getRegistrationFee() != null) {
            if (request.getRegistrationFee().signum() < 0) {
                throw new BizException("报名费不能为负数");
            }
            event.setRegistrationFee(request.getRegistrationFee());
        } else if (event.getRegistrationFee() == null) {
            event.setRegistrationFee(java.math.BigDecimal.ZERO);
        }
        validateEventTimeRange(event.getStartTime(), event.getEndTime());
    }

    private void validateEventTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BizException("结束时间不能早于开始时间");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.length() <= 10) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        }
        if (value.length() <= 16) {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private int boolToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
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
