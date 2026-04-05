package com.iprody.xpayment.adapter.app.checkstate.handler;

import java.util.UUID;

public interface PaymentStatusCheckHandler {
    /**
     * Проверяет статус платежа в X Payment Provider по заданному
     идентификатору. Если статус нетерминальный, то метод возвращает
     false. В противном случае, отправляет асинхронное уведомление
     Payment Service o измененном статус платежа и возвращает true.
     *
     * @param paymentGuid UUID платежа для проверки
     * @return true, если платеж завершен и новые проверки статуса
    не требуются, иначе false
     */
    boolean handle(UUID paymentGuid);
}
