package com.tgs.ecommerce.audit.dto;

import com.tgs.ecommerce.audit.domain.AuditAction;
import java.time.Instant;

/**
 * Representación pública de un registro de auditoría.
 */
public record AuditLogResponse(
    Long id,
    AuditAction action,
    String entityType,
    Long entityId,
    String performedBy,
    Instant performedAt,
    String details
) {}
