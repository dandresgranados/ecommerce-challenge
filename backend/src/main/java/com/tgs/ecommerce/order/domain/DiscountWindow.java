package com.tgs.ecommerce.order.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ventana de tiempo parametrizada durante la cual se aplica un descuento.
 *
 * <p>El administrador puede crear ventanas GLOBAL (10 %) y RANDOM (50 %)
 * con distintos rangos {@code start_at} — {@code end_at}. El {@code
 * DiscountService} las consulta al calcular el total de una orden.
 *
 * <p>Ejemplo: "Del 25/12 al 26/12 hay 10 % en todas las órdenes" →
 * DiscountWindow(type=GLOBAL, rate=0.10, startAt=..., endAt=...).
 */
@Entity
@Table(name = "discount_windows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountWindow extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre descriptivo, ej: "Black Friday 2026". */
    @Column(nullable = false, length = 128)
    private String name;

    /** Tipo de descuento. Persistido como texto. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DiscountWindowType type;

    /**
     * Porcentaje de descuento como fracción (0.10 = 10 %, 0.50 = 50 %).
     * BigDecimal para evitar errores de coma flotante en el cálculo.
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    /** Inicio de la ventana (inclusive). */
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    /** Fin de la ventana (exclusive). */
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    /** Permite desactivar sin borrar. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscountWindow other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}