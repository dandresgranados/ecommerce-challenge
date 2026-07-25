package com.tgs.ecommerce.common.exception;

/**
 * Se lanza cuando una entidad solicitada no existe.
 * El {@code GlobalExceptionHandler} la traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, Object id) {
        return new ResourceNotFoundException("%s con id=%s no encontrado".formatted(entity, id));
    }
}
