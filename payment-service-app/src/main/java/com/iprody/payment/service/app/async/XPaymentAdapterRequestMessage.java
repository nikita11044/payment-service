package com.iprody.payment.service.app.async;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class XPaymentAdapterRequestMessage implements Message {

    private UUID messageId;

    private UUID paymentId;

    private BigDecimal amount;

    private String currency;

    private Instant occurredAt;
}
