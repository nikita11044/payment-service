package com.iprody.xpayment.adapter.app.async.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.iprody.xpayment.adapter.app.async.AsyncListener;
import com.iprody.xpayment.adapter.app.async.MessageHandler;
import com.iprody.xpayment.adapter.app.async.XPaymentAdapterRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaXPaymentAdapterRequestListenerAdapter implements AsyncListener<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterRequestListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterRequestMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.x-payment-adapter.request}",
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
                message.getPaymentId(), record.partition(),
                record.offset()
            );
            onMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error(
                "Error handling XPayment Adapter request for paymentGuid={}",
                message.getPaymentId(),
                e
            );
            throw e;
        }
    }
}
