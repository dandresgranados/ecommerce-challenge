package com.tgs.ecommerce.audit.service;

import com.tgs.ecommerce.audit.domain.AuditLog;
import com.tgs.ecommerce.audit.dto.AuditLogResponse;

public final class AuditLogMapper {

    private AuditLogMapper() {}

    public static AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
            a.getId(),
            a.getAction(),
            a.getEntityType(),
            a.getEntityId(),
            a.getPerformedBy(),
            a.getPerformedAt(),
            a.getDetails()
        );
    }
}
