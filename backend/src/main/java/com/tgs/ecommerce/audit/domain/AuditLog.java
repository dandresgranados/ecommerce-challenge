package com.tgs.ecommerce.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de auditoría de un evento del sistema.
 *
 * <p>Es una entidad de <strong>solo escritura desde el código</strong>: se
 * crea, se lee, pero nunca se modifica ni borra (por eso no hereda de
 * AuditableEntity — sus propios metadatos internos serían redundantes).
 *
 * <p>Ejemplos:
 * <pre>
 * PAY   | Order   | 42  | admin | 2026-07-26T10:15Z | "orderNumber=ORD-... total=71.60"
 * UPDATE| Product | 5   | admin | 2026-07-26T10:16Z | "price 19.99 -> 24.99"
 * LOGIN | User    | 2   | user  | 2026-07-26T10:20Z | null
 * </pre>
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_entity",     columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_performed_at", columnList = "performed_at"),
        @Index(name = "idx_audit_action",     columnList = "action")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de acción realizada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuditAction action;

    /** Nombre corto del tipo de entidad afectada ({@code "Order"}, {@code "Product"}...). */
    @Column(name = "entity_type", length = 64)
    private String entityType;

    /** Id de la entidad afectada. Puede ser null (p. ej. LOGIN_FAILED de user desconocido). */
    @Column(name = "entity_id")
    private Long entityId;

    /** Username del actor. "system" cuando no hay usuario en el SecurityContext. */
    @Column(name = "performed_by", nullable = false, length = 64)
    private String performedBy;

    /** Momento del evento en UTC. Nunca cambia. */
    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    /**
     * Detalles adicionales en texto libre. Puede ser una descripción legible
     * ({@code "price 19.99 -> 24.99"}) o un JSON serializado — dependiendo
     * de quién lo llame. Nullable.
     */
    @Column(length = 2000)
    private String details;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
