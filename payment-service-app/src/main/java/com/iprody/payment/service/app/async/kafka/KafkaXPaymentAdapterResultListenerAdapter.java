package com.iprody.payment.service.app.async.kafka;

import com.iprody.payment.service.app.async.AsyncListener;
import com.iprody.payment.service.app.async.MessageHandler;
import com.iprody.payment.service.app.async.XPaymentAdapterResponseMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterResultListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;
    private final KafkaTemplate<String, XPaymentAdapterResponseMessage> template;

    @Value("${app.kafka.topics.xpayment-adapter.response-dlt}")
    private String deadLetterTopic;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.xpayment-adapter.response}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
        XPaymentAdapterResponseMessage message,
        ConsumerRecord<String, XPaymentAdapterResponseMessage> record,
        Acknowledgment ack
    ) {
        try {
            log.info(
                "Received XPayment Adapter response: paymentGuid={}, status={}, partition={}, offset={}",
                message.getPaymentGuid(), message.getStatus(),
                record.partition(), record.offset()
            );

            final List<String> violations = validate(message);
            if (!violations.isEmpty()) {
                log.warn(
                    "Validation failed for paymentGuid={}, sending to DLT. Violations: {}",
                    message.getPaymentGuid(), violations
                );
                template.send(deadLetterTopic, String.valueOf(message.getPaymentGuid()), message);
                ack.acknowledge();
                return;
            }

            onMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error(
                "Error handling XPayment Adapter response for paymentGuid={}",
                message.getPaymentGuid(), e
            );
            throw e;
        }
    }

    private List<String> validate(XPaymentAdapterResponseMessage message) {
        final List<String> violations = new ArrayList<>();

        if (message.getAmount() == null) {
            violations.add("Amount is required");
        }

        if (message.getCurrency() == null || message.getCurrency().isBlank()) {
            violations.add("Currency code is required");
        }

        if (!violations.isEmpty()) {
            return violations;
        }

        if (message.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            violations.add("Amount must not be negative: " + message.getAmount());
        }

        try {
            final Currency currency = Currency.getInstance(message.getCurrency());
            final int allowedFractionDigits = currency.getDefaultFractionDigits();

            if (allowedFractionDigits >= 0 && message.getAmount().scale() > allowedFractionDigits) {
                violations.add(String.format(
                    "Amount scale %d exceeds allowed %d fraction digits for currency %s (ISO 4217)",
                    message.getAmount().scale(), allowedFractionDigits, message.getCurrency()
                ));
            }
        } catch (IllegalArgumentException e) {
            violations.add("Unknown currency code: " + message.getCurrency());
        }

        return violations;
    }
}
