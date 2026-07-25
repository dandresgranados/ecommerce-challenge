package com.tgs.ecommerce.product.dto;

import java.math.BigDecimal;

/**
 * Criterios de búsqueda de productos, todos opcionales.
 *
 * <p>Spring MVC bindea automáticamente los query params de la URL a los
 * componentes del {@code record} (por nombre). Ejemplos:
 * <pre>
 * GET /api/products?name=teclado
 * GET /api/products?categoryId=1&minPrice=50&maxPrice=200
 * GET /api/products?active=true&page=0&size=20&sort=price,desc
 * </pre>
 *
 * <p>La paginación ({@code page}, {@code size}, {@code sort}) NO va en
 * este record: Spring la resuelve por separado con el argumento
 * {@code Pageable} del controller.
 */
public record ProductSearchCriteria(
    String name,
    Long categoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean active
) {

    /** Aplica trim al nombre; devuelve null si queda vacío. */
    public String normalizedName() {
        if (name == null) return null;
        String t = name.trim();
        return t.isEmpty() ? null : t;
    }
}
