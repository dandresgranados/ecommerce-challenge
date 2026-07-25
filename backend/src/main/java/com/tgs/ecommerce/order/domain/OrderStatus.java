package com.tgs.ecommerce.order.domain;

/**
 * Estados posibles de una orden.
 *
 * <p>Se persiste como texto en la BD (via {@code @Enumerated(EnumType.STRING)})
 * para que sea legible y resistente a reordenamientos.
 */
public enum OrderStatus {
    /** Orden creada, aún no pagada. */
    CREATED,
    /** Orden pagada. */
    PAID,
    /** Orden cancelada — no consume inventario. */
    CANCELED
}