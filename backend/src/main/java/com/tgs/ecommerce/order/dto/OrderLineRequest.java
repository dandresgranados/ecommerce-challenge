package com.tgs.ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Línea de una orden en el request de creación.
 * No lleva precio — el servicio hace snapshot desde el Product actual.
 */
public record OrderLineRequest(

    @NotNull(message = "productId es obligatorio")
    Long productId,

    @NotNull
    @Positive(message = "quantity debe ser >= 1")
    Integer quantity
) {}
