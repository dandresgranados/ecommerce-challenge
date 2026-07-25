package com.tgs.ecommerce.product.service;

import com.tgs.ecommerce.product.domain.Product;
import com.tgs.ecommerce.product.dto.ProductResponse;

/**
 * Conversión Product → ProductResponse.
 *
 * <p>El {@code stock} se pasa por parámetro (o {@code null}) porque
 * consultar el {@code Inventory} para cada producto en un listado
 * generaría N+1 queries. El servicio decide cuándo enriquecer con stock.
 */
public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product p, Integer stock) {
        return new ProductResponse(
            p.getId(),
            p.getSku(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getActive(),
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,
            stock,
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

    public static ProductResponse toResponse(Product p) {
        return toResponse(p, null);
    }
}
