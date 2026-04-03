package com.iprody.xpayment.adapter.app.async.kafka;

import com.iprody.xpayment.adapter.app.async.AsyncSender;
import com.iprody.xpayment.adapter.app.async.XPaymentAdapterResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaXPaymentAdapterResponseSender implements AsyncSender<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterResponseSender.class);

    private final KafkaTemplate<String, XPaymentAdapterResponseMessage> template;
    private final String topic;

    public KafkaXPaymentAdapterResponseSender(
        KafkaTemplate<String, XPaymentAdapterResponseMessage> template,
        @Value("${app.kafka.topics.xpayment-adapter.response:xpayment-adapter.responses}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterResponseMessage msg) {
        final String key = msg.getPaymentGuid().toString();
        log.info(
            "Sending XPayment Adapter response: guid={}, amount={}, currency={} -> topic={}",
            msg.getPaymentGuid(), msg.getAmount(), msg.getCurrency(), topic
        );
        template.send(topic, key, msg);
    }
}
