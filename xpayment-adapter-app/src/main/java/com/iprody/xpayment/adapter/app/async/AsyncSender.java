package com.iprody.xpayment.adapter.app.async;

public interface AsyncSender<T extends Message> {
    void send(T message);
}
