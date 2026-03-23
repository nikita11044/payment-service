package com.iprody.payment.service.app.async.kafka;

import com.iprody.payment.service.app.async.AsyncSender;
import com.iprody.payment.service.app.async.XPaymentAdapterRequestMessage;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaXPaymentAdapterRequestSender implements AsyncSender<XPaymentAdapterRequestMessage> {
    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterRequestSender.class);
    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> template;
    private final String topic;

    public KafkaXPaymentAdapterRequestSender(
        KafkaTemplate<String, XPaymentAdapterRequestMessage> template,
        @Value("${app.kafka.topics.xpayment-adapter.request:xpayment-adapter.requests}")
        String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage msg) {
        final String key = msg.getPaymentId().toString();
        log.info(
            "Sending XPayment Adapter request: guid={}, amount={}, currency={} -> topic={}",
            msg.getPaymentId(), msg.getAmount(),
            msg.getCurrency(), topic
        );
        template.send(topic, key, msg);
    }
}
