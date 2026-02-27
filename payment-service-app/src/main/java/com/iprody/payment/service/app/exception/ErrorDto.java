package com.iprody.payment.service.app.exception;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ErrorDto {

    private final UUID entityId;

    private Operation operation;

    private String errorMessage;

    private Instant timestamp;
}
