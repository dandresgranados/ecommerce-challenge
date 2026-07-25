package com.tgs.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representación pública de un producto. Incluye información resumida de
 * la categoría (id + nombre) para evitar que el frontend tenga que hacer
 * una segunda petición para mostrarla.
 *
 * <p>Campo {@code stock}: unidades actualmente disponibles según el
 * inventario asociado. Puede ser {@code null} si no se ha cargado (por
 * ejemplo, en listados donde no queremos JOIN con inventory).
 */
public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Boolean active,
    Long categoryId,
    String categoryName,
    Integer stock,
    Instant createdAt,
    Instant updatedAt
) {}
