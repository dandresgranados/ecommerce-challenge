package com.tgs.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload del endpoint {@code POST /api/auth/login}.
 *
 * <p>Se usa {@code record} porque es inmutable y el propósito es puramente
 * transportar datos entre HTTP y el service.
 */
public record LoginRequest(

    @NotBlank(message = "El username es obligatorio")
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    String password
) {}
