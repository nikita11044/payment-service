package com.iprody.xpayment.adapter.app.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record ChargeRequestDto(
    BigDecimal amount,
    String currency,
    String customer,
    UUID order,
    String receiptEmail,
    Map<String, Object> metadata
) {
}
