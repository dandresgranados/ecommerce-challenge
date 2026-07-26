package com.tgs.ecommerce.audit.domain;

/**
 * Acciones auditables del sistema.
 *
 * <p>Se guardan como texto en la BD (via {@code @Enumerated(EnumType.STRING)})
 * para que el log sea legible incluso desde una consola SQL sin tener el
 * código Java a mano.
 */
public enum AuditAction {
    /** Login exitoso. */
    LOGIN,
    /** Intento de login con credenciales inválidas. */
    LOGIN_FAILED,
    /** Registro público de un nuevo usuario. */
    REGISTER,
    /** Creación de una entidad (Product, Category, User, Order, ...). */
    CREATE,
    /** Actualización de una entidad. */
    UPDATE,
    /** Borrado (físico o soft) de una entidad. */
    DELETE,
    /** Cambio de contraseña de un usuario. */
    PASSWORD_CHANGE,
    /** Pago de una orden (CREATED → PAID). */
    PAY,
    /** Cancelación de una orden (CREATED → CANCELED). */
    CANCEL
}
