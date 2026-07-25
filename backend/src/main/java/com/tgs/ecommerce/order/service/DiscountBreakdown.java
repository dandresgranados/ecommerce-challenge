package com.tgs.ecommerce.order.service;

import java.math.BigDecimal;

/**
 * Desglose del descuento aplicado a una orden.
 *
 * <p>Cada componente puede ser 0. La suma se llama {@code totalRate()} y
 * se acota en {@link DiscountCalculator} a 0.9500 máximo para que
 * ninguna orden llegue a total = 0.
 *
 * <p>Se devuelve por el {@link DiscountCalculator} y también se expone en
 * la respuesta de la orden (útil para auditar y para que el frontend
 * muestre "aplicado: 10% global + 5% cliente frecuente").
 */
public record DiscountBreakdown(
    BigDecimal globalRate,       // 0.10 si hay ventana GLOBAL activa
    BigDecimal randomRate,       // 0.50 si randomOrder + ventana RANDOM
    BigDecimal loyaltyRate,      // 0.05 si es cliente frecuente
    BigDecimal totalRate         // suma acotada
) {}
