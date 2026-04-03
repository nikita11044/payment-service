package com.iprody.xpayment.adapter.app.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ChargeResponseDto(
    UUID id,
    BigDecimal amount,
    String currency,
    BigDecimal amountReceived,
    String createdAt,
    String chargedAt,
    String customer,
    UUID order,
    String receiptEmail,
    String status,
    Map<String, Object> metadata
) {
}
