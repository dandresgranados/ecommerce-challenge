package com.tgs.ecommerce.report.dto;

import java.math.BigDecimal;

/**
 * Proyección para el reporte de clientes frecuentes.
 * Incluye contador de órdenes y total gastado (excluyendo canceladas).
 */
public interface FrequentCustomerProjection {
    Long getUserId();
    String getUsername();
    String getFullName();
    String getEmail();
    Long getOrderCount();
    BigDecimal getTotalSpent();
}
