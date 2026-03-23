package com.iprody.xpayment.adapter.app.async;

public interface AsyncListener<T extends Message> {
    void onMessage(T message);
}
