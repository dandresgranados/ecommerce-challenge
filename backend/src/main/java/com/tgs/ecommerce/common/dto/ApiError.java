package com.tgs.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Cuerpo estándar de las respuestas de error del API. Todos los endpoints
 * fallidos devuelven esta estructura para que el frontend pueda mostrar los
 * mensajes de forma consistente.
 *
 * <p>{@code @JsonInclude(NON_NULL)} omite del JSON los campos nulos —
 * p. ej. {@code fieldErrors} solo aparece cuando hay errores de validación.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    Instant timestamp,
    int status,
    String error,        // ej. "Bad Request", "Conflict"
    String message,      // mensaje principal legible
    String path,         // ruta del endpoint
    List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {}
}
