package com.tgs.ecommerce.order.service;

import com.tgs.ecommerce.order.domain.Order;
import com.tgs.ecommerce.order.domain.OrderItem;
import com.tgs.ecommerce.order.dto.OrderLineResponse;
import com.tgs.ecommerce.order.dto.OrderResponse;
import com.tgs.ecommerce.order.dto.OrderResponse.DiscountBreakdownDto;
import java.math.BigDecimal;

public final class OrderMapper {

    private OrderMapper() {}

    /**
     * @param order  la orden (relaciones LAZY deben estar dentro de la sesión)
     * @param breakdown desglose calculado; puede ser null si no se dispone
     *        (por ejemplo, en un listado histórico donde no recalculamos)
     */
    public static OrderResponse toResponse(Order order, DiscountBreakdown breakdown) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getUser() != null ? order.getUser().getUsername() : null,
            order.getStatus(),
            order.getRandomOrder(),
            order.getSubtotal(),
            order.getDiscountRate(),
            order.getTotal(),
            breakdown != null
                ? new DiscountBreakdownDto(
                    breakdown.globalRate(), breakdown.randomRate(),
                    breakdown.loyaltyRate(), breakdown.totalRate())
                : reconstructBreakdown(order),
            order.getItems().stream().map(OrderMapper::toLineResponse).toList(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    private static OrderLineResponse toLineResponse(OrderItem item) {
        return new OrderLineResponse(
            item.getId(),
            item.getProduct() != null ? item.getProduct().getId() : null,
            item.getProductName(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineTotal()
        );
    }

    /**
     * Reconstruye el desglose leyendo los rates individuales persistidos
     * en la propia entidad {@link Order}. Se usa cuando no tenemos un
     * {@link DiscountBreakdown} recién calculado (listados históricos).
     */
    private static DiscountBreakdownDto reconstructBreakdown(Order order) {
        return new DiscountBreakdownDto(
            order.getDiscountGlobalRate(),
            order.getDiscountRandomRate(),
            order.getDiscountLoyaltyRate(),
            order.getDiscountRate());
    }
}
