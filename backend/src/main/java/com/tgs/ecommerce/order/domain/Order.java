package com.tgs.ecommerce.order.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import com.tgs.ecommerce.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Orden (pedido) realizada por un usuario.
 *
 * <p>Nota importante: la tabla se llama {@code orders} (plural), no
 * {@code order}, porque {@code ORDER} es palabra reservada en SQL.
 *
 * <p>Los importes se almacenan como snapshots (subtotal, discountRate,
 * total) para que la orden histórica no cambie si el precio de un
 * producto se modifica más tarde.
 *
 * <p>La colección de líneas ({@code OrderItem}) se añadirá en el
 * siguiente paso, con {@code @OneToMany(mappedBy = "order",
 * cascade = ALL, orphanRemoval = true)}.
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_number", columnList = "order_number", unique = true),
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador legible para el usuario (ej. "ORD-2026-000123").
     * Generado por el servicio, no por la BD. Único.
     */
    @Column(name = "order_number", nullable = false, length = 32)
    private String orderNumber;

    /** Usuario que realizó la orden. LAZY para no cargarlo salvo que se necesite. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Suma de las líneas antes de aplicar descuentos.
     * BigDecimal(19,4): 19 dígitos totales, 4 decimales.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    /**
     * Porcentaje total de descuento aplicado a esta orden.
     * Rango 0.0000 (sin descuento) a 0.6500 (10% ventana + 50% aleatorio + 5% frecuente).
     * Se guarda para poder auditar/reproducir el cálculo.
     */
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    /** Componente del descuento total aportado por la ventana GLOBAL activa. */
    @Column(name = "discount_global_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountGlobalRate = BigDecimal.ZERO;

    /** Componente aportado por la ventana RANDOM (si el usuario marcó pedido aleatorio). */
    @Column(name = "discount_random_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountRandomRate = BigDecimal.ZERO;

    /** Componente aportado por ser cliente frecuente. */
    @Column(name = "discount_loyalty_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal discountLoyaltyRate = BigDecimal.ZERO;

    /**
     * Importe final ya con el descuento aplicado.
     * total = subtotal * (1 - discountRate). Se persiste (aunque sea
     * derivable) para acelerar reportes y evitar recálculos.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    /**
     * Marca si el usuario pidió la función "pedido aleatorio", que
     * dispara la evaluación del 50% de descuento cuando la orden cae
     * dentro de la ventana temporal configurada.
     */
    @Column(name = "random_order", nullable = false)
    @Builder.Default
    private Boolean randomOrder = false;

    /** Estado de la orden. Se guarda como texto para legibilidad. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    /** Control de concurrencia optimista. */
    @Version
    private Long version;

    /**
     * Líneas de la orden. Order es el aggregate root: cascade = ALL para
     * persistir/actualizar/borrar los items junto con la orden;
     * orphanRemoval = true para que quitar un item de la colección lo
     * borre físicamente de la BD.
     */
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ------------------------------------------------------------
    // Helpers para mantener consistencia bidireccional de la relación.
    // Siempre llamar a estos en vez de items.add(...) directamente.
    // ------------------------------------------------------------

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
