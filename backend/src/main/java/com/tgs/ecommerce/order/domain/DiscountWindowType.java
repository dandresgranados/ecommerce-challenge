package com.tgs.ecommerce.order.domain;

/**
 * Tipos de ventana de descuento definidos en el reto:
 * <ul>
 *   <li>{@link #GLOBAL}: descuento del 10% aplicado a TODAS las órdenes
 *       creadas dentro del rango de tiempo.</li>
 *   <li>{@link #RANDOM}: descuento del 50% aplicado a órdenes marcadas como
 *       "pedido aleatorio", pero solo si caen dentro del rango.</li>
 * </ul>
 */
public enum DiscountWindowType {
    GLOBAL,
    RANDOM
}