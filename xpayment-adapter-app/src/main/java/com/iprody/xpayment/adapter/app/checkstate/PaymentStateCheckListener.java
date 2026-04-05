package com.iprody.xpayment.adapter.app.checkstate;

import com.iprody.xpayment.adapter.app.checkstate.handler.PaymentStatusCheckHandler;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentStateCheckListener {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final String dlxExchangeName;
    private final String dlxRoutingKey;
    private final PaymentStatusCheckHandler paymentStatusCheckHandler;

    @Value("${app.rabbitmq.max-retries:60}")
    private int maxRetries;

    @Value("${app.rabbitmq.interval-ms:60000}")
    private long intervalMs;

    public PaymentStateCheckListener(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey,
        @Value("${app.rabbitmq.dlx-exchange-name}") String dlxExchangeName,
        @Value("${app.rabbitmq.dlx-routing-key}") String dlxRoutingKey,
        PaymentStatusCheckHandler paymentStatusCheckHandler
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.dlxExchangeName = dlxExchangeName;
        this.dlxRoutingKey = dlxRoutingKey;
        this.paymentStatusCheckHandler = paymentStatusCheckHandler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handle(PaymentCheckStateMessage message, Message raw) {
        final MessageProperties props = raw.getMessageProperties();
        final int retryCount = (int) props.getHeaders().getOrDefault("x-retry-count", 0);

        final boolean paid = paymentStatusCheckHandler.handle(message.chargeGuid());

        if (paid) {
            return;
        }

        if (retryCount < maxRetries) {
            republishWithDelay(message, retryCount);
        } else {
            sendToDeadLetter(message, retryCount, props.getConsumerQueue());
        }
    }

    private void republishWithDelay(PaymentCheckStateMessage message, int retryCount) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, m -> {
            m.getMessageProperties().setHeader("x-delay", intervalMs);
            m.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
            return m;
        });
    }

    private void sendToDeadLetter(PaymentCheckStateMessage message, int retryCount, String originalQueue) {
        rabbitTemplate.convertAndSend(dlxExchangeName, dlxRoutingKey, message, m -> {
            m.getMessageProperties().setHeader("x-retry-count", retryCount);
            m.getMessageProperties().setHeader("x-final-status", "TIMEOUT");
            m.getMessageProperties().setHeader("x-original-queue", originalQueue);
            return m;
        });
    }
}
