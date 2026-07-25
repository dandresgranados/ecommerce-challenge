package com.tgs.ecommerce.product.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Producto del catálogo.
 *
 * <p>Relación {@link Category}: many-to-one (varios productos pertenecen a una
 * categoría). Usamos FetchType.LAZY explícito para evitar la trampa del
 * default EAGER de @ManyToOne.
 *
 * <p>{@code @Version}: control de concurrencia optimista. Hibernate incrementa
 * este campo en cada UPDATE. Si dos peticiones intentan modificar la misma
 * fila a la vez, la segunda falla con OptimisticLockException. Evita
 * corrupciones al modificar precios o activar/desactivar productos.
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_sku", columnList = "sku", unique = true),
        @Index(name = "idx_products_active", columnList = "active"),
        @Index(name = "idx_products_category", columnList = "category_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SKU = código único de referencia del producto. */
    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    /**
     * Precio unitario. NUNCA usar double para dinero — errores de coma
     * flotante corrompen totales. BigDecimal + precisión explícita.
     * precision=19: hasta 19 dígitos totales. scale=4: 4 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    /** Activo/inactivo (soft-delete, para el reporte "productos activos"). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Categoría a la que pertenece el producto. LAZY explícito para no
     * cargar Category cada vez que consultamos un producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Control de concurrencia optimista. */
    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}