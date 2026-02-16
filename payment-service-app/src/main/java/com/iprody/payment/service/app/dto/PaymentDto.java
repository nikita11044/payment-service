package com.iprody.payment.service.app.dto;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentDto {
    private UUID guid;
    private UUID inquiryRefId;
    private BigDecimal amount;
    private String currency;
    private UUID transactionRefId;
    private PaymentStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
