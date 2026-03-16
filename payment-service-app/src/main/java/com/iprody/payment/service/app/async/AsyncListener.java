package com.iprody.payment.service.app.async;

public interface AsyncListener<T extends Message> {
    void onMessage(T message);
}
