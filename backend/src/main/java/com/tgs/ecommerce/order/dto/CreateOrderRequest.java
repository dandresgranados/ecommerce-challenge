package com.tgs.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Payload para {@code POST /api/orders}.
 *
 * <p>{@code randomOrder}: si el usuario pidió la función de "pedido
 * aleatorio". Combinado con una {@code DiscountWindow(type=RANDOM)}
 * activa, aplica el 50% de descuento.
 *
 * <p>{@code @Valid} en items propaga la validación de {@link
 * OrderLineRequest} a cada línea.
 */
public record CreateOrderRequest(

    @NotEmpty(message = "La orden debe tener al menos una línea")
    @Valid
    List<OrderLineRequest> items,

    Boolean randomOrder
) {

    public boolean isRandomOrder() {
        return Boolean.TRUE.equals(randomOrder);
    }
}
