package com.tgs.ecommerce.user.service;

import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.dto.UserResponse;

/**
 * Conversiones entre entidades y DTOs del módulo user.
 *
 * <p>Se implementa como clase con métodos estáticos (sin estado). Para
 * proyectos más grandes se usaría MapStruct — aquí no compensa la
 * complejidad extra.
 */
public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        return new UserResponse(
            u.getId(),
            u.getUsername(),
            u.getEmail(),
            u.getFullName(),
            u.getActive(),
            u.getRoles().stream().map(r -> r.getName().name()).toList(),
            u.getCreatedAt(),
            u.getUpdatedAt()
        );
    }
}
