package com.tgs.ecommerce.user.domain;

/**
 * Roles del sistema. Se guardan como texto en la BD (via
 * {@code @Enumerated(EnumType.STRING)} en la entidad {@link Role}) para
 * que sean legibles y no rompan si se reordenan.
 */
public enum RoleName {
    /** Administrador — puede gestionar usuarios, productos, inventario. */
    ADMIN,
    /** Usuario final — puede consultar y realizar órdenes. */
    USER
}
