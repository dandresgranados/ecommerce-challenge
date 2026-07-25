package com.tgs.ecommerce.report.dto;

/**
 * Proyección para el reporte de productos más vendidos.
 *
 * <p>Los nombres de los getters deben coincidir con los alias del
 * {@code SELECT} en la query JPQL correspondiente.
 */
public interface TopSellingProductProjection {
    Long getProductId();
    String getSku();
    String getName();
    Long getTotalSold();
}
