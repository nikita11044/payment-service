package com.iprody.payment.service.app.async;

public interface MessageHandler<T extends Message> {
    void handle(T message);
}
