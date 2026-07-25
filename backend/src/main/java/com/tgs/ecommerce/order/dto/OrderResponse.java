package com.tgs.ecommerce.order.dto;

import com.tgs.ecommerce.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Representación pública de una orden.
 *
 * <p>Además de los campos del dominio, incluye el {@code discountBreakdown}
 * para transparencia con el usuario/frontend: puede mostrar
 * "10% ventana global + 5% cliente frecuente = 15% de descuento".
 */
public record OrderResponse(
    Long id,
    String orderNumber,
    Long userId,
    String username,
    OrderStatus status,
    Boolean randomOrder,
    BigDecimal subtotal,
    BigDecimal discountRate,
    BigDecimal total,
    DiscountBreakdownDto discountBreakdown,
    List<OrderLineResponse> items,
    Instant createdAt,
    Instant updatedAt
) {

    /** Espejo público del DiscountBreakdown interno del service. */
    public record DiscountBreakdownDto(
        BigDecimal globalRate,
        BigDecimal randomRate,
        BigDecimal loyaltyRate,
        BigDecimal totalRate
    ) {}
}
