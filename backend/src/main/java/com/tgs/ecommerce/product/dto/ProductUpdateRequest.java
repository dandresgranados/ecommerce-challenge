package com.tgs.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload para {@code PUT /api/products/{id}}.
 *
 * <p>Actualización parcial: los campos nulos NO se tocan. El SKU no se
 * puede cambiar tras la creación (para no romper trazabilidad).
 */
public record ProductUpdateRequest(

    @Size(min = 2, max = 128)
    String name,

    @Size(max = 512)
    String description,

    @DecimalMin(value = "0.0000", inclusive = false, message = "El precio debe ser mayor que 0")
    BigDecimal price,

    Long categoryId,

    Boolean active
) {}
