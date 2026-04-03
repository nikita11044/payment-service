package com.iprody.xpayment.adapter.app.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentStateCheckRegister {
    void register(
        UUID chargeGuid,
        UUID paymentGuid,
        BigDecimal amount,
        String currency
    );
}
