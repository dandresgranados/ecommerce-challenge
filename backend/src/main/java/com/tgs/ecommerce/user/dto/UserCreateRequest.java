package com.tgs.ecommerce.user.dto;

import com.tgs.ecommerce.user.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Payload para {@code POST /api/users} — solo ADMIN puede crear usuarios
 * arbitrarios con roles específicos (a diferencia del registro público,
 * que solo asigna rol USER).
 */
public record UserCreateRequest(

    @NotBlank
    @Size(min = 3, max = 64)
    String username,

    @NotBlank
    @Email
    @Size(max = 128)
    String email,

    @NotBlank
    @Size(min = 6, max = 100)
    String password,

    @Size(max = 128)
    String fullName,

    @NotEmpty(message = "Debe asignarse al menos un rol")
    Set<RoleName> roles
) {}
