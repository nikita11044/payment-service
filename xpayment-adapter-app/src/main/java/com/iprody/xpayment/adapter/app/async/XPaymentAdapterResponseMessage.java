package com.iprody.xpayment.adapter.app.async;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class XPaymentAdapterResponseMessage implements Message {

    private UUID messageId;

    private UUID paymentGuid;

    private BigDecimal amount;

    private String currency;

    private UUID transactionRefId;

    private XPaymentAdapterStatus status;

    private Instant occurredAt;
}
