package com.tgs.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload para {@code POST /api/products}.
 *
 * <p>Al crear un producto se crea también su registro de {@code Inventory}
 * en la misma transacción, con {@code initialStock} unidades (default 0).
 */
public record ProductRequest(

    @NotBlank
    @Size(min = 2, max = 64)
    String sku,

    @NotBlank
    @Size(min = 2, max = 128)
    String name,

    @Size(max = 512)
    String description,

    @NotNull
    @DecimalMin(value = "0.0000", inclusive = false, message = "El precio debe ser mayor que 0")
    BigDecimal price,

    @NotNull(message = "categoryId es obligatorio")
    Long categoryId,

    Boolean active,

    @PositiveOrZero(message = "El stock inicial debe ser >= 0")
    Integer initialStock,

    @PositiveOrZero(message = "El stock mínimo debe ser >= 0")
    Integer minStock
) {}
