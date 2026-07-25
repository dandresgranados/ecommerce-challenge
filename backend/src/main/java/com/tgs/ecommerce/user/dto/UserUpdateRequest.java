package com.tgs.ecommerce.user.dto;

import com.tgs.ecommerce.user.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Payload para {@code PUT /api/users/{id}}.
 *
 * <p>Campos opcionales: solo se actualizan los que llegan no-nulos. La
 * contraseña se cambia por un endpoint aparte para no arrastrarla en cada
 * actualización.
 */
public record UserUpdateRequest(

    @Email
    @Size(max = 128)
    String email,

    @Size(max = 128)
    String fullName,

    Boolean active,

    Set<RoleName> roles
) {}
