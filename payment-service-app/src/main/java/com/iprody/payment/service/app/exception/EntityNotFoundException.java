package com.iprody.payment.service.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final Operation operation;

    private final UUID entityId;

    public EntityNotFoundException(String message, Operation operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
    }
}
