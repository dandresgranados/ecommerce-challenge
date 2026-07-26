package com.tgs.ecommerce.audit.dto;

import com.tgs.ecommerce.audit.domain.AuditAction;
import java.time.Instant;

/**
 * Filtros opcionales de {@code GET /api/audit-logs}.
 *
 * <p>Ejemplos:
 * <pre>
 * /api/audit-logs?entityType=Order&entityId=42
 * /api/audit-logs?performedBy=admin
 * /api/audit-logs?action=PAY&from=2026-01-01T00:00:00Z
 * </pre>
 */
public record AuditLogSearchCriteria(
    String entityType,
    Long entityId,
    String performedBy,
    AuditAction action,
    Instant from,
    Instant to
) {}
