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
    public static final String DEAD_TOPIC = "hysaas-email-dead";
    private static final int MAX_RETRY = 3;

    private final ConfigService configService;
    private final EmailTemplateService emailTemplateService;
    private final EmailLogService emailLogService;
    private final RocketMQTemplate rocketMQTemplate;

    public void sendTemplate(Long tenantId, Long eventId, String code, String to, Map<String, String> vars) {
        try {
            doSend(tenantId, eventId, code, to, vars, 0);
        } catch (Exception e) {
            log.warn("email send failed, enqueue retry: {} -> {}", code, to, e);
            rocketMQTemplate.convertAndSend(RETRY_TOPIC, new EmailMessage(tenantId, eventId, code, to, vars, 0));
        }
    }

    public void sendTest(String to) {
        String host = configService.getValue("smtpHost");
        String user = configService.getValue("smtpUser");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(user)) {
            throw new IllegalStateException("SMTP 未配置");
        }
        String subject = "HySaaS 邮件测试";
        String body = "这是一封 SMTP 测试邮件，收到说明配置正确。";
        JavaMailSenderImpl sender = buildSender();
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(user);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
            emailLogService.record(null, null, "TEST", to, subject, "SENT", null, 0);
            log.info("test email sent -> {}", to);
        } catch (Exception e) {
            emailLogService.record(null, null, "TEST", to, subject, "FAILED", e.getMessage(), 0);
            throw new IllegalStateException("测试邮件发送失败", e);
        }
    }

    public void resend(EmailMessage message) {
        doSend(message.getTenantId(), message.getEventId(), message.getCode(),
                message.getTo(), message.getVars(), message.getRetryCount());
    }

    public void handleRetryFailed(EmailMessage message, Exception e) {
        int next = message.getRetryCount() + 1;
        if (next >= MAX_RETRY) {
            emailLogService.record(message.getTenantId(), message.getEventId(), message.getCode(),
                    message.getTo(), null, "DEAD", e.getMessage(), next);
            rocketMQTemplate.convertAndSend(DEAD_TOPIC, message);
            log.error("email dead letter: {} -> {}", message.getCode(), message.getTo(), e);
            return;
        }
        message.setRetryCount(next);
        rocketMQTemplate.convertAndSend(RETRY_TOPIC, message);
        log.warn("email retry scheduled {}/{}: {} -> {}", next, MAX_RETRY, message.getCode(), message.getTo());
    }

    private void doSend(Long tenantId, Long eventId, String code, String to, Map<String, String> vars, int retryCount) {
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
            emailLogService.record(tenantId, eventId, code, to, null, "FAILED", "template not found", retryCount);
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
            emailLogService.record(tenantId, eventId, code, to, subject, "SENT", null, retryCount);
            log.info("email sent: {} -> {}", code, to);
        } catch (Exception e) {
            emailLogService.record(tenantId, eventId, code, to, subject, "FAILED", e.getMessage(), retryCount);
            throw new IllegalStateException("send email failed", e);
        }
    }

    private JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(configService.getValue("smtpHost"));
        String port = configService.getValue("smtpPort");
        int portNum = 587;
        if (StringUtils.hasText(port)) {
            portNum = Integer.parseInt(port);
            sender.setPort(portNum);
        }
        sender.setUsername(configService.getValue("smtpUser"));
        sender.setPassword(configService.getValue("smtpPass"));
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (portNum == 465 || portNum == 994) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(portNum));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        return sender;
    }
}
