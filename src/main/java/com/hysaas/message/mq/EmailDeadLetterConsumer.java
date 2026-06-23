package com.hysaas.message.mq;

import com.hysaas.message.dto.EmailMessage;
import com.hysaas.message.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = EmailService.DEAD_TOPIC, consumerGroup = "hysaas-email-dead-consumer")
public class EmailDeadLetterConsumer implements RocketMQListener<EmailMessage> {

    @Override
    public void onMessage(EmailMessage message) {
        log.error("email dead letter archived: {} -> {}", message.getCode(), message.getTo());
    }
}
