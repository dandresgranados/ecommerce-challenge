package com.tgs.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload del endpoint {@code POST /api/auth/register}.
 * Registro público — el usuario se crea con rol USER.
 */
public record RegisterRequest(

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 64)
    String username,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email con formato inválido")
    @Size(max = 128)
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    String password,

    @Size(max = 128)
    String fullName
) {}
