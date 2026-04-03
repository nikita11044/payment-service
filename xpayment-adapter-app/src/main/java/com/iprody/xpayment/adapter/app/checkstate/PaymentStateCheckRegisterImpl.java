package com.iprody.xpayment.adapter.app.checkstate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentStateCheckRegisterImpl implements PaymentStateCheckRegister {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    @Value("${app.rabbitmq.max-retries:60}")
    private int maxRetries;

    @Value("${app.rabbitmq.interval-ms:60000}")
    private long intervalMs;

    public PaymentStateCheckRegisterImpl(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.delayed-exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void register(UUID chargeGuid, UUID paymentGuid, BigDecimal amount, String currency) {
        final var message = new PaymentCheckStateMessage(chargeGuid, paymentGuid, amount, currency);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, m -> {
            m.getMessageProperties().setHeader("x-delay", intervalMs);
            m.getMessageProperties().setHeader("x-retry-count", 1);
            return m;
        });
    }
}
