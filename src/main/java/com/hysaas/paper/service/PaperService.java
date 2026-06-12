package com.hysaas.paper.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.common.exception.BizException;
import com.hysaas.event.entity.EvtEvent;
import com.hysaas.event.mapper.EvtEventMapper;
import com.hysaas.paper.dto.PaperAssignRequest;
import com.hysaas.paper.dto.PaperSubmissionVO;
import com.hysaas.paper.dto.PortalDraftRequest;
import com.hysaas.paper.dto.ReviewSubmitRequest;
import com.hysaas.paper.dto.ReviewTaskVO;
import com.hysaas.paper.entity.PaperReview;
import com.hysaas.paper.entity.PaperReviewAssignment;
import com.hysaas.paper.entity.PaperSubmission;
import com.hysaas.paper.mapper.PaperReviewAssignmentMapper;
import com.hysaas.paper.mapper.PaperReviewMapper;
import com.hysaas.paper.mapper.PaperSubmissionMapper;
import com.hysaas.system.entity.SysUser;
import com.hysaas.system.mapper.SysUserMapper;
import com.hysaas.system.service.EnterpriseRoleService;
import com.hysaas.system.support.EnterpriseContext;
import com.hysaas.system.support.PortalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaperService {

    private static final Set<String> PORTAL_EVENT_STATUSES = Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED");
    private static final Set<String> FINAL_STATUSES = Set.of("ACCEPTED", "REJECTED", "REVISION");

    private final PaperSubmissionMapper paperSubmissionMapper;
    private final PaperReviewMapper paperReviewMapper;
    private final PaperReviewAssignmentMapper paperReviewAssignmentMapper;
    private final EvtEventMapper evtEventMapper;
    private final SysUserMapper sysUserMapper;
    private final EnterpriseContext enterpriseContext;
    private final PortalContext portalContext;
    private final EnterpriseRoleService enterpriseRoleService;

    public PageResult<PaperSubmissionVO> enterpriseList(String status, Integer page, Integer size) {
        enterpriseContext.requireTenantId();
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        LambdaQueryWrapper<PaperSubmission> wrapper = new LambdaQueryWrapper<PaperSubmission>()
                .orderByDesc(PaperSubmission::getSubmittedAt)
                .orderByDesc(PaperSubmission::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(PaperSubmission::getStatus, status);
        }
        Page<PaperSubmission> result = paperSubmissionMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        List<PaperSubmissionVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    @Transactional
    public PaperSubmissionVO assign(Long paperId, PaperAssignRequest request) {
        if (request.getExpertId() == null) {
            throw new BizException("请选择专家");
        }
        Long tenantId = enterpriseContext.requireTenantId();
        PaperSubmission paper = requireEnterprisePaper(paperId);
        if (!Set.of("SUBMITTED", "RESUBMITTED").contains(paper.getStatus())) {
            throw new BizException("当前状态不可分配专家");
        }
        SysUser expert = requireExpert(request.getExpertId(), tenantId);
        clearReviewData(paperId);
        PaperReviewAssignment assignment = new PaperReviewAssignment();
        assignment.setTenantId(tenantId);
        assignment.setSubmissionId(paperId);
        assignment.setReviewerId(expert.getId());
        assignment.setAssignedAt(LocalDateTime.now());
        paperReviewAssignmentMapper.insert(assignment);
        paper.setStatus("UNDER_REVIEW");
        paper.setUpdatedAt(LocalDateTime.now());
        paperSubmissionMapper.updateById(paper);
        return toVO(paper);
    }

    @Transactional
    public PaperSubmissionVO finalizePaper(Long paperId, String status) {
        if (!FINAL_STATUSES.contains(status)) {
            throw new BizException("无效的终审状态");
        }
        enterpriseContext.requireTenantId();
        PaperSubmission paper = requireEnterprisePaper(paperId);
        if (!"REVIEW_DONE".equals(paper.getStatus())) {
            throw new BizException("仅评审完成的稿件可终审");
        }
        paper.setStatus(status);
        paper.setUpdatedAt(LocalDateTime.now());
        paperSubmissionMapper.updateById(paper);
        return toVO(paper);
    }

    public List<ReviewTaskVO> reviewTasks() {
        Long tenantId = enterpriseContext.requireTenantId();
        long reviewerId = StpUtil.getLoginIdAsLong();
        List<PaperReviewAssignment> assignments = paperReviewAssignmentMapper.selectList(
                new LambdaQueryWrapper<PaperReviewAssignment>()
                        .eq(PaperReviewAssignment::getTenantId, tenantId)
                        .eq(PaperReviewAssignment::getReviewerId, reviewerId));
        return assignments.stream()
                .filter(a -> !hasReview(a.getSubmissionId(), reviewerId))
                .map(a -> {
                    PaperSubmission paper = paperSubmissionMapper.selectById(a.getSubmissionId());
                    if (paper == null || !"UNDER_REVIEW".equals(paper.getStatus())) {
                        return null;
                    }
                    ReviewTaskVO vo = new ReviewTaskVO();
                    vo.setPaperId(paper.getId());
                    vo.setTitle(paper.getTitle());
                    vo.setAuthor(paper.getAuthor());
                    vo.setDeadline(formatDeadline(a.getAssignedAt()));
                    return vo;
                })
                .filter(vo -> vo != null)
                .toList();
    }

    @Transactional
    public void submitReview(Long paperId, ReviewSubmitRequest request) {
        Long tenantId = enterpriseContext.requireTenantId();
        long reviewerId = StpUtil.getLoginIdAsLong();
        PaperSubmission paper = requireEnterprisePaper(paperId);
        if (!"UNDER_REVIEW".equals(paper.getStatus())) {
            throw new BizException("稿件不在评审中");
        }
        PaperReviewAssignment assignment = paperReviewAssignmentMapper.selectOne(
                new LambdaQueryWrapper<PaperReviewAssignment>()
                        .eq(PaperReviewAssignment::getSubmissionId, paperId)
                        .eq(PaperReviewAssignment::getReviewerId, reviewerId)
                        .last("LIMIT 1"));
        if (assignment == null) {
            throw new BizException("您未被分配该稿件");
        }
        if (hasReview(paperId, reviewerId)) {
            throw new BizException("已提交过评审");
        }
        PaperReview review = new PaperReview();
        review.setTenantId(tenantId);
        review.setSubmissionId(paperId);
        review.setReviewerId(reviewerId);
        review.setComment(request.getComment());
        review.setSuggest(StringUtils.hasText(request.getSuggest()) ? request.getSuggest() : "accept");
        review.setReviewedAt(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());
        paperReviewMapper.insert(review);
        paper.setStatus("REVIEW_DONE");
        paper.setUpdatedAt(LocalDateTime.now());
        paperSubmissionMapper.updateById(paper);
    }

    public List<PaperSubmissionVO> portalList() {
        SysUser user = portalContext.requireAttendee();
        List<PaperSubmission> papers = paperSubmissionMapper.selectList(new LambdaQueryWrapper<PaperSubmission>()
                .eq(PaperSubmission::getUserId, user.getId())
                .orderByDesc(PaperSubmission::getUpdatedAt));
        return papers.stream().map(this::toVO).toList();
    }

    @Transactional
    public PaperSubmissionVO saveDraft(PortalDraftRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BizException("标题不能为空");
        }
        SysUser user = portalContext.requireAttendee();
        Long eventId = resolvePaperEventId(user.getTenantId());
        PaperSubmission paper = paperSubmissionMapper.selectOne(new LambdaQueryWrapper<PaperSubmission>()
                .eq(PaperSubmission::getUserId, user.getId())
                .eq(PaperSubmission::getEventId, eventId)
                .eq(PaperSubmission::getStatus, "DRAFT")
                .last("LIMIT 1"));
        if (paper == null) {
            paper = new PaperSubmission();
            paper.setTenantId(user.getTenantId());
            paper.setEventId(eventId);
            paper.setUserId(user.getId());
            paper.setTitle(request.getTitle());
            paper.setAuthor(user.getNickname());
            paper.setStatus("DRAFT");
            paper.setVersion(1);
            paper.setCreatedAt(LocalDateTime.now());
            paper.setUpdatedAt(LocalDateTime.now());
            paperSubmissionMapper.insert(paper);
        } else {
            paper.setTitle(request.getTitle());
            paper.setUpdatedAt(LocalDateTime.now());
            paperSubmissionMapper.updateById(paper);
        }
        return toVO(paper);
    }

    @Transactional
    public PaperSubmissionVO submitPaper(Long id) {
        SysUser user = portalContext.requireAttendee();
        PaperSubmission paper = paperSubmissionMapper.selectById(id);
        if (paper == null || !user.getId().equals(paper.getUserId())) {
            throw new BizException(404, "稿件不存在");
        }
        if ("DRAFT".equals(paper.getStatus())) {
            paper.setStatus("SUBMITTED");
            paper.setSubmittedAt(LocalDateTime.now());
        } else if ("REVISION".equals(paper.getStatus())) {
            paper.setStatus("RESUBMITTED");
            paper.setVersion(paper.getVersion() + 1);
            paper.setSubmittedAt(LocalDateTime.now());
            clearReviewData(id);
        } else {
            throw new BizException("当前状态不可提交");
        }
        paper.setUpdatedAt(LocalDateTime.now());
        paperSubmissionMapper.updateById(paper);
        return toVO(paper);
    }

    private void clearReviewData(Long submissionId) {
        paperReviewAssignmentMapper.delete(new LambdaQueryWrapper<PaperReviewAssignment>()
                .eq(PaperReviewAssignment::getSubmissionId, submissionId));
        paperReviewMapper.delete(new LambdaQueryWrapper<PaperReview>()
                .eq(PaperReview::getSubmissionId, submissionId));
    }

    private boolean hasReview(Long submissionId, Long reviewerId) {
        return paperReviewMapper.selectCount(new LambdaQueryWrapper<PaperReview>()
                .eq(PaperReview::getSubmissionId, submissionId)
                .eq(PaperReview::getReviewerId, reviewerId)) > 0;
    }

    private SysUser requireExpert(Long expertId, Long tenantId) {
        SysUser user = sysUserMapper.selectById(expertId);
        if (user == null || !tenantId.equals(user.getTenantId()) || !"ENTERPRISE".equals(user.getUserType())) {
            throw new BizException("专家不存在");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new BizException("专家账号已禁用");
        }
        if (!enterpriseRoleService.getRoleCodes(expertId).contains("EXPERT")) {
            throw new BizException("所选成员不是专家角色");
        }
        return user;
    }

    private PaperSubmission requireEnterprisePaper(Long paperId) {
        enterpriseContext.requireTenantId();
        PaperSubmission paper = paperSubmissionMapper.selectById(paperId);
        if (paper == null) {
            throw new BizException(404, "稿件不存在");
        }
        return paper;
    }

    private Long resolvePaperEventId(Long tenantId) {
        EvtEvent event = evtEventMapper.selectOne(new LambdaQueryWrapper<EvtEvent>()
                .eq(EvtEvent::getTenantId, tenantId)
                .eq(EvtEvent::getPaperEnabled, 1)
                .in(EvtEvent::getStatus, PORTAL_EVENT_STATUSES)
                .orderByDesc(EvtEvent::getCreatedAt)
                .last("LIMIT 1"));
        if (event == null) {
            throw new BizException("暂无开放论文投稿的活动，请先开启活动论文功能并发布");
        }
        return event.getId();
    }

    private String formatDeadline(LocalDateTime assignedAt) {
        if (assignedAt == null) {
            return "";
        }
        return assignedAt.plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private PaperSubmissionVO toVO(PaperSubmission paper) {
        PaperSubmissionVO vo = new PaperSubmissionVO();
        BeanUtils.copyProperties(paper, vo);
        return vo;
    }
}
