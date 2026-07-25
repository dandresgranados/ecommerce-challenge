package com.tgs.ecommerce.order.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import com.tgs.ecommerce.product.domain.Product;
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
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea de una orden — un producto con su cantidad, precio unitario y total.
 *
 * <p><strong>Patrón snapshot:</strong> {@code unitPrice} y {@code productName}
 * se copian desde {@link Product} en el momento de crear la orden. Si el
 * precio o el nombre cambian después, esta línea histórica NO se altera.
 *
 * <p>El lado dueño de la relación es esta clase (tiene la FK {@code order_id}).
 * En {@link Order} declararemos el lado inverso con {@code @OneToMany
 * (mappedBy = "order")}.
 */
@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(name = "idx_order_items_order", columnList = "order_id"),
        @Index(name = "idx_order_items_product", columnList = "product_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Orden a la que pertenece esta línea. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Producto vendido (referencia, no snapshot). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Snapshot del nombre del producto al momento de la venta. */
    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    /** Snapshot del precio unitario. Congelado para la vida de la orden. */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    /** Cantidad comprada. Debe ser >= 1 (validado en el service). */
    @Column(nullable = false)
    private Integer quantity;

    /** Total de la línea = unitPrice * quantity. Persistido para reportes. */
    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
