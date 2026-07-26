package com.tgs.ecommerce.audit.repository;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.domain.AuditLog;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados compuestos para búsqueda dinámica de {@link AuditLog}.
 * Se combinan con {@code Specification.allOf(...)} en el service.
 */
public final class AuditLogSpecifications {

    private AuditLogSpecifications() {}

    public static Specification<AuditLog> hasEntityType(String type) {
        return (root, q, cb) -> cb.equal(root.get("entityType"), type);
    }

    public static Specification<AuditLog> hasEntityId(Long id) {
        return (root, q, cb) -> cb.equal(root.get("entityId"), id);
    }

    public static Specification<AuditLog> performedBy(String username) {
        return (root, q, cb) -> cb.equal(root.get("performedBy"), username);
    }

    public static Specification<AuditLog> hasAction(AuditAction action) {
        return (root, q, cb) -> cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> performedAfter(Instant from) {
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("performedAt"), from);
    }

    public static Specification<AuditLog> performedBefore(Instant to) {
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("performedAt"), to);
    }
}
