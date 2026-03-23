package com.iprody.payment.service.app.async.kafka;

import com.iprody.payment.service.app.async.AsyncListener;
import com.iprody.payment.service.app.async.MessageHandler;
import com.iprody.payment.service.app.async.XPaymentAdapterResponseMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterResultListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

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
}
