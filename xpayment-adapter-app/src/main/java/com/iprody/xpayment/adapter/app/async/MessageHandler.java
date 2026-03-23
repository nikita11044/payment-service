package com.iprody.xpayment.adapter.app.async;

public interface MessageHandler<T extends Message> {
    void handle(T message);
}
