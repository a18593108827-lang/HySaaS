package com.hysaas.message.service;

import com.hysaas.message.dto.EmailMessage;
import com.hysaas.message.entity.MsgEmailTemplate;
import com.hysaas.system.service.ConfigService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    public static final String RETRY_TOPIC = "hysaas-email-retry";

    private final ConfigService configService;
    private final EmailTemplateService emailTemplateService;
    private final RocketMQTemplate rocketMQTemplate;

    public void sendTemplate(Long tenantId, Long eventId, String code, String to, Map<String, String> vars) {
        try {
            doSend(tenantId, eventId, code, to, vars);
        } catch (Exception e) {
            log.warn("email send failed, enqueue retry: {} -> {}", code, to, e);
            rocketMQTemplate.convertAndSend(RETRY_TOPIC, new EmailMessage(tenantId, eventId, code, to, vars));
        }
    }

    public void resend(EmailMessage message) {
        doSend(message.getTenantId(), message.getEventId(), message.getCode(), message.getTo(), message.getVars());
    }

    private void doSend(Long tenantId, Long eventId, String code, String to, Map<String, String> vars) {
        if (!StringUtils.hasText(to)) {
            return;
        }
        String host = configService.getValue("smtpHost");
        String user = configService.getValue("smtpUser");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(user)) {
            log.info("smtp not configured, skip email {} -> {}", code, to);
            return;
        }
        MsgEmailTemplate template = emailTemplateService.resolve(tenantId, eventId, code);
        if (template == null) {
            log.warn("email template not found: {}", code);
            return;
        }
        String body = emailTemplateService.render(template.getBody(), vars);
        String subject = emailTemplateService.render(template.getSubject(), vars);
        JavaMailSenderImpl sender = buildSender();
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(user);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
            log.info("email sent: {} -> {}", code, to);
        } catch (Exception e) {
            throw new IllegalStateException("send email failed", e);
        }
    }

    private JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(configService.getValue("smtpHost"));
        String port = configService.getValue("smtpPort");
        if (StringUtils.hasText(port)) {
            sender.setPort(Integer.parseInt(port));
        }
        sender.setUsername(configService.getValue("smtpUser"));
        sender.setPassword(configService.getValue("smtpPass"));
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
