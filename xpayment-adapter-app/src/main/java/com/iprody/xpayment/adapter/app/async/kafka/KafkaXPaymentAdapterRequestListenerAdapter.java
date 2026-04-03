package com.iprody.xpayment.adapter.app.async.kafka;

import com.iprody.xpayment.adapter.app.async.AsyncListener;
import com.iprody.xpayment.adapter.app.async.MessageHandler;
import com.iprody.xpayment.adapter.app.async.XPaymentAdapterRequestMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterRequestListenerAdapter implements AsyncListener<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterRequestListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterRequestMessage> handler;
    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> template;

    @Value("${app.kafka.topics.xpayment-adapter.request-dlt}")
    private String deadLetterTopic;

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.xpayment-adapter.request}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
        ConsumerRecord<String, XPaymentAdapterRequestMessage> record,
        Acknowledgment ack
    ) {
        final XPaymentAdapterRequestMessage message = record.value();
        try {
            log.info(
                "Received XPayment Adapter request: paymentGuid={}, partition={}, offset={}",
                message.getPaymentId(), record.partition(), record.offset()
            );

            final List<String> violations = validate(message);
            if (!violations.isEmpty()) {
                log.warn(
                    "Validation failed for paymentGuid={}, sending to DLT. Violations: {}",
                    message.getPaymentId(), violations
                );
                template.send(deadLetterTopic, String.valueOf(message.getPaymentId()), message);
                ack.acknowledge();
                return;
            }

            onMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error(
                "Error handling XPayment Adapter request for paymentGuid={}",
                message.getPaymentId(), e
            );
            throw e;
        }
    }

    private List<String> validate(XPaymentAdapterRequestMessage message) {
        final List<String> violations = new ArrayList<>();

        if (message.getAmount() == null) {
            violations.add("Amount is required");
        }

        if (message.getCurrency() == null || message.getCurrency().isBlank()) {
            violations.add("Currency is required");
        }

        if (!violations.isEmpty()) {
            return violations;
        }

        if (message.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            violations.add("Amount must not be negative: " + message.getAmount());
        }

        try {
            final Currency curr = Currency.getInstance(message.getCurrency());
            final int allowedFractionDigits = curr.getDefaultFractionDigits();

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
