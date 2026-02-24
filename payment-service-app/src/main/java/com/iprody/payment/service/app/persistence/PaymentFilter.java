package com.iprody.payment.service.app.persistence;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFilter (
    String currency,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    Instant createdAfter,
    Instant createdBefore,
    PaymentStatus status
) { }
