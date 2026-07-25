package com.tgs.ecommerce.user.dto;

import java.time.Instant;
import java.util.List;

/**
 * Representación pública de un usuario — segura para devolver por API.
 *
 * <p>NO incluye la contraseña ni información interna sensible.
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    String fullName,
    Boolean active,
    List<String> roles,
    Instant createdAt,
    Instant updatedAt
) {}
