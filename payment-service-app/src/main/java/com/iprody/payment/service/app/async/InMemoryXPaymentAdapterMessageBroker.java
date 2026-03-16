package com.iprody.payment.service.app.async;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;

@Service
@RequiredArgsConstructor
public class InMemoryXPaymentAdapterMessageBroker implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        final UUID txId = UUID.randomUUID();

        scheduler.schedule(() -> emit(request, txId, XPaymentAdapterStatus.PROCESSING), 0, TimeUnit.SECONDS);
        scheduler.schedule(() -> emit(request, txId, XPaymentAdapterStatus.PROCESSING), 10, TimeUnit.SECONDS);
        scheduler.schedule(() -> emit(request, txId, XPaymentAdapterStatus.SUCCEEDED), 20, TimeUnit.SECONDS);
    }

    private void emit(XPaymentAdapterRequestMessage request, UUID txId, XPaymentAdapterStatus status) {
        final XPaymentAdapterResponseMessage result = new XPaymentAdapterResponseMessage();
        result.setPaymentGuid(request.getPaymentId());
        result.setAmount(request.getAmount());
        result.setCurrency(request.getCurrency());
        result.setTransactionRefId(txId);
        result.setStatus(status);
        result.setOccurredAt(Instant.now());
        resultListener.onMessage(result);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
