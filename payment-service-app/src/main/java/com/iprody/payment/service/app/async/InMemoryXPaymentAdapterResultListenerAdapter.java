package com.iprody.payment.service.app.async;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InMemoryXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterResponseMessage> {

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage msg) {
        handler.handle(msg);
    }

}
