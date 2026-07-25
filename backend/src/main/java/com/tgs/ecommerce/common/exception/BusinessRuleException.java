package com.tgs.ecommerce.common.exception;

/**
 * Se lanza cuando la operación viola una regla de negocio (ej. username
 * duplicado, stock insuficiente, orden en estado no editable).
 * El {@code GlobalExceptionHandler} la traduce a HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
