package com.tgs.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para {@code POST /api/categories} y {@code PUT /api/categories/{id}}.
 *
 * <p>El servicio valida además que {@code name} no exista ya en la BD (regla
 * de negocio, no de formato).
 */
public record CategoryRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 64, message = "El nombre debe tener entre 2 y 64 caracteres")
    String name,

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String description
) {}
