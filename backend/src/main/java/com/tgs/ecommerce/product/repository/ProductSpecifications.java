package com.tgs.ecommerce.product.repository;

import com.tgs.ecommerce.product.domain.Product;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

/**
 * Colección de {@link Specification} para {@link Product}.
 *
 * <p>Cada método devuelve un {@code Specification<Product>} que representa
 * un fragmento del {@code WHERE}. Se combinan con {@code .and(...)} en el
 * {@code ProductService} según qué filtros vengan rellenos.
 *
 * <p>Ejemplo:
 * <pre>
 * Specification&lt;Product&gt; spec = Specification.allOf(
 *     hasNameLike("teclado"),
 *     inCategory(1L),
 *     priceBetween(new BigDecimal("50"), new BigDecimal("200"))
 * );
 * </pre>
 */
public final class ProductSpecifications {

    private ProductSpecifications() {}

    /** name ILIKE %text% (case-insensitive). */
    public static Specification<Product> hasNameLike(String text) {
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), pattern);
    }

    /** category_id = :id */
    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) ->
            cb.equal(root.get("category").get("id"), categoryId);
    }

    /** price >= :min */
    public static Specification<Product> priceGte(BigDecimal min) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    /** price &lt;= :max */
    public static Specification<Product> priceLte(BigDecimal max) {
        return (root, query, cb) ->
            cb.lessThanOrEqualTo(root.get("price"), max);
    }

    /** active = :flag */
    public static Specification<Product> isActive(Boolean flag) {
        return (root, query, cb) -> cb.equal(root.get("active"), flag);
    }
}
