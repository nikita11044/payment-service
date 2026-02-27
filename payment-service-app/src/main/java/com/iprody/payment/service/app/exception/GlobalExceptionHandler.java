package com.iprody.payment.service.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleNotFound(EntityNotFoundException ex) {
        return ErrorDto.builder()
                .errorMessage(ex.getMessage())
                .operation(ex.getOperation())
                .entityId(ex.getEntityId())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleOther(Exception ex) {
        return ErrorDto.builder()
                .errorMessage(ex.getMessage())
                .build();
    }
}
