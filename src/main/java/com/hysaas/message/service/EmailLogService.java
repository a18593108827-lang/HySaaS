package com.hysaas.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hysaas.common.constant.CommonConstants;
import com.hysaas.common.dto.PageResult;
import com.hysaas.message.dto.EmailLogVO;
import com.hysaas.message.entity.MsgEmailLog;
import com.hysaas.message.mapper.MsgEmailLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailLogService {

    private final MsgEmailLogMapper msgEmailLogMapper;

    public void record(Long tenantId, Long eventId, String code, String recipient,
                       String subject, String status, String errorMsg, int retryCount) {
        MsgEmailLog log = new MsgEmailLog();
        log.setTenantId(tenantId);
        log.setEventId(eventId);
        log.setCode(code);
        log.setRecipient(recipient);
        log.setSubject(subject);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setRetryCount(retryCount);
        log.setCreatedAt(java.time.LocalDateTime.now());
        msgEmailLogMapper.insert(log);
    }

    public PageResult<EmailLogVO> pageByTenant(Long tenantId, Integer page, Integer size) {
        return page(new LambdaQueryWrapper<MsgEmailLog>()
                .eq(MsgEmailLog::getTenantId, tenantId), page, size);
    }

    public PageResult<EmailLogVO> pageAll(Integer page, Integer size) {
        return page(new LambdaQueryWrapper<MsgEmailLog>(), page, size);
    }

    private PageResult<EmailLogVO> page(LambdaQueryWrapper<MsgEmailLog> wrapper, Integer page, Integer size) {
        int pageNum = page == null ? CommonConstants.DEFAULT_PAGE : page;
        int pageSize = size == null ? CommonConstants.DEFAULT_SIZE : Math.min(size, CommonConstants.MAX_SIZE);
        wrapper.orderByDesc(MsgEmailLog::getCreatedAt);
        Page<MsgEmailLog> result = msgEmailLogMapper.selectPage(new Page<>(pageNum + 1L, pageSize), wrapper);
        List<EmailLogVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, result.getTotal());
    }

    private EmailLogVO toVO(MsgEmailLog log) {
        EmailLogVO vo = new EmailLogVO();
        vo.setId(log.getId());
        vo.setCode(log.getCode());
        vo.setRecipient(log.getRecipient());
        vo.setSubject(log.getSubject());
        vo.setStatus(log.getStatus());
        vo.setErrorMsg(log.getErrorMsg());
        vo.setRetryCount(log.getRetryCount());
        if (log.getCreatedAt() != null) {
            vo.setCreatedAt(log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return vo;
    }
}
