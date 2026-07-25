package com.tgs.ecommerce.report.dto;

import java.math.BigDecimal;

/**
 * Fila del reporte "Top N clientes frecuentes".
 */
public record FrequentCustomerResponse(
    Long userId,
    String username,
    String fullName,
    String email,
    long orderCount,
    BigDecimal totalSpent
) {

    public static FrequentCustomerResponse from(FrequentCustomerProjection p) {
        return new FrequentCustomerResponse(
            p.getUserId(),
            p.getUsername(),
            p.getFullName(),
            p.getEmail(),
            p.getOrderCount() != null ? p.getOrderCount() : 0L,
            p.getTotalSpent() != null ? p.getTotalSpent() : BigDecimal.ZERO
        );
    }
}
