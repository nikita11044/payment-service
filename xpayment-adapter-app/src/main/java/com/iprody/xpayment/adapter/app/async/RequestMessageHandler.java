package com.iprody.xpayment.adapter.app.async;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        scheduler.schedule(() -> {
            final XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
            responseMessage.setPaymentGuid(message.getMessageId());
            responseMessage.setAmount(message.getAmount());
            responseMessage.setCurrency(message.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.SUCCEEDED);
            responseMessage.setTransactionRefId(UUID.randomUUID());
            responseMessage.setOccurredAt(Instant.now());
            sender.send(responseMessage);
        }, 10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
