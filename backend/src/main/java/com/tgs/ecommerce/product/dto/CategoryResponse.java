package com.tgs.ecommerce.product.dto;

import java.time.Instant;

/**
 * Representación pública de una categoría para responder por API.
 */
public record CategoryResponse(
    Long id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
