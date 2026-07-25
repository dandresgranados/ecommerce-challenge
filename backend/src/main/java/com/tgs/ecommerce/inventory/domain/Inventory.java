package com.tgs.ecommerce.inventory.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import com.tgs.ecommerce.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inventario de un producto: cuánto stock hay y cuál es el mínimo permitido.
 *
 * <p>Relación 1:1 con {@link Product}: cada producto tiene exactamente un
 * registro de inventario. La FK product_id es UNIQUE para materializar la
 * unicidad a nivel de BD.
 *
 * <p>{@code @Version} para evitar race conditions al decrementar stock
 * (dos órdenes simultáneas comprando el mismo producto).
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Producto al que pertenece este stock. LAZY para no traerlo siempre. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    /** Unidades disponibles actualmente. Debe ser >= 0 (validado en el service). */
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    /** Stock mínimo — al llegar a este umbral se podría avisar. */
    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    private Integer minStock = 0;

    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}