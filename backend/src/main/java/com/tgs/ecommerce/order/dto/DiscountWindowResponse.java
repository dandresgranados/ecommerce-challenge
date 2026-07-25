package com.tgs.ecommerce.order.dto;

import com.tgs.ecommerce.order.domain.DiscountWindowType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representación pública de una ventana de descuento.
 */
public record DiscountWindowResponse(
    Long id,
    String name,
    DiscountWindowType type,
    BigDecimal rate,
    Instant startAt,
    Instant endAt,
    Boolean active,
    Instant createdAt,
    Instant updatedAt
) {}
