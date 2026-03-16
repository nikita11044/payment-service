package com.iprody.payment.service.app.async;

public interface AsyncSender<T extends Message> {
    void send(T message);
}
