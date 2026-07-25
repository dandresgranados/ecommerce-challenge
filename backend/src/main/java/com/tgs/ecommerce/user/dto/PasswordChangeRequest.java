package com.tgs.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para {@code POST /api/users/{id}/password}.
 * Un usuario puede cambiar su propia contraseña; un ADMIN puede cambiar cualquiera.
 */
public record PasswordChangeRequest(

    @NotBlank
    @Size(min = 6, max = 100)
    String newPassword
) {}
