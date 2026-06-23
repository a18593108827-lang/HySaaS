package com.hysaas.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.entity.EvtRegistration;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.event.mapper.EvtRegistrationMapper;
import com.hysaas.paper.entity.PaperSubmission;
import com.hysaas.paper.mapper.PaperSubmissionMapper;
import com.hysaas.system.dto.AdminDashboardStatsVO;
import com.hysaas.system.dto.EnterpriseDashboardStatsVO;
import com.hysaas.system.entity.SysTenant;
import com.hysaas.system.mapper.SysTenantMapper;
import com.hysaas.system.support.EnterpriseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Set<String> ACTIVE_EVENT_STATUSES = Set.of(
            "PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");
    private static final Set<String> PENDING_PAPER_STATUSES = Set.of(
            "SUBMITTED", "RESUBMITTED", "REVIEW_DONE");

    private final SysTenantMapper sysTenantMapper;
    private final EvtEventMapper evtEventMapper;
    private final EvtRegistrationMapper evtRegistrationMapper;
    private final PaperSubmissionMapper paperSubmissionMapper;
    private final EnterpriseContext enterpriseContext;

    public AdminDashboardStatsVO adminStats() {
        AdminDashboardStatsVO vo = new AdminDashboardStatsVO();
        vo.setPending(sysTenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getStatus, "PENDING")));
        vo.setApproved(sysTenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getStatus, "APPROVED")));
        vo.setTotal(sysTenantMapper.selectCount(new LambdaQueryWrapper<>()));
        return vo;
    }

    public EnterpriseDashboardStatsVO enterpriseStats() {
        Long tenantId = enterpriseContext.requireTenantId();
        EnterpriseDashboardStatsVO vo = new EnterpriseDashboardStatsVO();
        vo.setEvents(evtEventMapper.selectCount(new LambdaQueryWrapper<EvtEvent>()
                .eq(EvtEvent::getTenantId, tenantId)
                .in(EvtEvent::getStatus, ACTIVE_EVENT_STATUSES)));
        vo.setPendingRegistrations(evtRegistrationMapper.selectCount(new LambdaQueryWrapper<EvtRegistration>()
                .eq(EvtRegistration::getTenantId, tenantId)
                .eq(EvtRegistration::getStatus, "PENDING")));
        vo.setPendingPapers(paperSubmissionMapper.selectCount(new LambdaQueryWrapper<PaperSubmission>()
                .eq(PaperSubmission::getTenantId, tenantId)
                .in(PaperSubmission::getStatus, PENDING_PAPER_STATUSES)));
        return vo;
    }
}
