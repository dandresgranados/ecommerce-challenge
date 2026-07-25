package com.tgs.ecommerce.order.dto;

import java.math.BigDecimal;

/**
 * Línea de una orden en la respuesta.
 * Contiene los snapshots congelados al momento de la venta.
 */
public record OrderLineResponse(
    Long id,
    Long productId,
    String productName,       // snapshot
    BigDecimal unitPrice,     // snapshot
    Integer quantity,
    BigDecimal lineTotal
) {}
