package com.tgs.ecommerce.order.dto;

import com.tgs.ecommerce.order.domain.DiscountWindowType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload para {@code POST /api/discount-windows} y
 * {@code PUT /api/discount-windows/{id}}.
 *
 * <p>La validación de coherencia {@code startAt < endAt} se hace en el
 * service (no con Bean Validation, porque involucra 2 campos).
 */
public record DiscountWindowRequest(

    @NotBlank
    @Size(min = 2, max = 128)
    String name,

    @NotNull(message = "type es obligatorio (GLOBAL o RANDOM)")
    DiscountWindowType type,

    @NotNull
    @DecimalMin(value = "0.0001", message = "El descuento debe ser mayor que 0")
    @DecimalMax(value = "0.9999", message = "El descuento no puede ser 100% o más")
    BigDecimal rate,

    @NotNull(message = "startAt es obligatorio (ISO-8601 UTC)")
    Instant startAt,

    @NotNull(message = "endAt es obligatorio (ISO-8601 UTC)")
    Instant endAt,

    Boolean active
) {}
