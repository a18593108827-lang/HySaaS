package com.hysaas.message.mq;

import com.hysaas.message.dto.EmailMessage;
import com.hysaas.message.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = EmailService.RETRY_TOPIC, consumerGroup = "hysaas-email-consumer")
public class EmailSendConsumer implements RocketMQListener<EmailMessage> {

    private final EmailService emailService;

    @Override
    public void onMessage(EmailMessage message) {
        try {
            emailService.resend(message);
        } catch (Exception e) {
            log.error("email retry failed: {}", message.getCode(), e);
        }
    }
}
