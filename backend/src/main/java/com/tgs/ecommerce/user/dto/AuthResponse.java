package com.tgs.ecommerce.user.dto;

import java.util.List;

/**
 * Respuesta de {@code /api/auth/login} y {@code /api/auth/register}.
 * Contiene el JWT ya firmado y la info mínima del usuario autenticado.
 *
 * <p>Nunca incluye el hash de contraseña ni datos sensibles.
 */
public record AuthResponse(
    String token,
    String tokenType,       // siempre "Bearer"
    long expiresInMs,
    UserResponse user,
    List<String> roles
) {}
