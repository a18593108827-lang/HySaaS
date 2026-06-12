package com.hysaas.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hysaas.common.exception.BizException;
import com.hysaas.message.dto.EmailTemplateVO;
import com.hysaas.message.entity.MsgEmailTemplate;
import com.hysaas.message.mapper.MsgEmailTemplateMapper;
import com.hysaas.system.support.EnterpriseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private static final Map<String, String> CODE_NAMES = new LinkedHashMap<>();

    static {
        CODE_NAMES.put("PAPER_SUBMITTED", "投稿提交");
        CODE_NAMES.put("PAPER_UNDER_REVIEW", "进入评审");
        CODE_NAMES.put("PAPER_ACCEPTED", "录用通知");
        CODE_NAMES.put("PAPER_REJECTED", "拒稿通知");
        CODE_NAMES.put("PAPER_REVISION", "需修改通知");
        CODE_NAMES.put("REG_APPROVED", "报名通过");
        CODE_NAMES.put("REG_REJECTED", "报名拒绝");
        CODE_NAMES.put("PAY_SUCCESS", "支付成功");
        CODE_NAMES.put("INVOICE_READY", "发票就绪");
    }

    private final MsgEmailTemplateMapper msgEmailTemplateMapper;
    private final EnterpriseContext enterpriseContext;

    public List<EmailTemplateVO> list() {
        Long tenantId = enterpriseContext.requireTenantId();
        ensureDefaults(tenantId);
        return msgEmailTemplateMapper.selectList(new LambdaQueryWrapper<MsgEmailTemplate>()
                        .eq(MsgEmailTemplate::getTenantId, tenantId)
                        .isNull(MsgEmailTemplate::getEventId)
                        .orderByAsc(MsgEmailTemplate::getCode))
                .stream().map(this::toVO).toList();
    }

    @Transactional
    public void update(Long id, String content) {
        Long tenantId = enterpriseContext.requireTenantId();
        MsgEmailTemplate template = msgEmailTemplateMapper.selectById(id);
        if (template == null || !tenantId.equals(template.getTenantId())) {
            throw new BizException(404, "模板不存在");
        }
        template.setBody(content);
        template.setUpdatedAt(LocalDateTime.now());
        msgEmailTemplateMapper.updateById(template);
    }

    public MsgEmailTemplate resolve(Long tenantId, Long eventId, String code) {
        if (eventId != null) {
            MsgEmailTemplate eventTpl = msgEmailTemplateMapper.selectOne(new LambdaQueryWrapper<MsgEmailTemplate>()
                    .eq(MsgEmailTemplate::getTenantId, tenantId)
                    .eq(MsgEmailTemplate::getEventId, eventId)
                    .eq(MsgEmailTemplate::getCode, code)
                    .last("LIMIT 1"));
            if (eventTpl != null) {
                return eventTpl;
            }
        }
        return msgEmailTemplateMapper.selectOne(new LambdaQueryWrapper<MsgEmailTemplate>()
                .eq(MsgEmailTemplate::getTenantId, tenantId)
                .isNull(MsgEmailTemplate::getEventId)
                .eq(MsgEmailTemplate::getCode, code)
                .last("LIMIT 1"));
    }

    public String render(String body, Map<String, String> vars) {
        if (!StringUtils.hasText(body) || vars == null) {
            return body;
        }
        String result = body;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private void ensureDefaults(Long tenantId) {
        long count = msgEmailTemplateMapper.selectCount(new LambdaQueryWrapper<MsgEmailTemplate>()
                .eq(MsgEmailTemplate::getTenantId, tenantId)
                .isNull(MsgEmailTemplate::getEventId));
        if (count > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, String> entry : CODE_NAMES.entrySet()) {
            MsgEmailTemplate template = new MsgEmailTemplate();
            template.setTenantId(tenantId);
            template.setCode(entry.getKey());
            template.setSubject(entry.getValue());
            template.setBody("尊敬的 {{name}}，关于 {{eventName}}：{{status}}");
            template.setCreatedAt(now);
            template.setUpdatedAt(now);
            msgEmailTemplateMapper.insert(template);
        }
    }

    private EmailTemplateVO toVO(MsgEmailTemplate template) {
        EmailTemplateVO vo = new EmailTemplateVO();
        vo.setId(template.getId());
        vo.setCode(template.getCode());
        vo.setName(CODE_NAMES.getOrDefault(template.getCode(), template.getCode()));
        vo.setContent(template.getBody());
        return vo;
    }
}
