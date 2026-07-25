package com.tgs.ecommerce.report.dto;

/**
 * Fila del reporte "Top N productos más vendidos".
 */
public record TopSellingProductResponse(
    Long productId,
    String sku,
    String name,
    long totalSold
) {

    public static TopSellingProductResponse from(TopSellingProductProjection p) {
        return new TopSellingProductResponse(
            p.getProductId(),
            p.getSku(),
            p.getName(),
            p.getTotalSold() != null ? p.getTotalSold() : 0L
        );
    }
}
