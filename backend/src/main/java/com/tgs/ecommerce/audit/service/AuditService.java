package com.tgs.ecommerce.audit.service;

import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.hasAction;
import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.hasEntityId;
import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.hasEntityType;
import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.performedAfter;
import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.performedBefore;
import static com.tgs.ecommerce.audit.repository.AuditLogSpecifications.performedBy;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.domain.AuditLog;
import com.tgs.ecommerce.audit.dto.AuditLogResponse;
import com.tgs.ecommerce.audit.dto.AuditLogSearchCriteria;
import com.tgs.ecommerce.audit.repository.AuditLogRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachada para escribir y consultar {@link AuditLog}.
 *
 * <p>Los otros services llaman a {@link #log(AuditAction, String, Long, String)}
 * tras realizar acciones importantes. El actor se resuelve automáticamente
 * desde el {@link SecurityContextHolder} — si no hay usuario autenticado,
 * se registra como {@code "system"}.
 *
 * <p>La escritura usa {@code REQUIRES_NEW} para que el log persista incluso
 * si el llamador hace rollback de su transacción... es decir, en la práctica
 * queremos que el log SIEMPRE quede. Sin embargo, para mantener simple el
 * ejemplo del reto, usamos la propagación por defecto (mismo trans que el
 * llamador). En producción se replantearía según el requisito de auditoría.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    // ------------------------------------------------------------
    // Escritura
    // ------------------------------------------------------------

    @Transactional
    public void log(AuditAction action, String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .performedBy(resolveUsername())
            .performedAt(Instant.now())
            .details(details)
            .build();
        repository.save(entry);
        log.debug("AUDIT {} {}[{}] by {} details={}",
            action, entityType, entityId, entry.getPerformedBy(), details);
    }

    /** Variante sin detalles. */
    @Transactional
    public void log(AuditAction action, String entityType, Long entityId) {
        log(action, entityType, entityId, null);
    }

    // ------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(AuditLogSearchCriteria c, Pageable pageable) {
        Specification<AuditLog> spec = Specification.allOf();
        if (c.entityType()  != null && !c.entityType().isBlank()) spec = spec.and(hasEntityType(c.entityType()));
        if (c.entityId()    != null)                              spec = spec.and(hasEntityId(c.entityId()));
        if (c.performedBy() != null && !c.performedBy().isBlank()) spec = spec.and(performedBy(c.performedBy()));
        if (c.action()      != null)                              spec = spec.and(hasAction(c.action()));
        if (c.from()        != null)                              spec = spec.and(performedAfter(c.from()));
        if (c.to()          != null)                              spec = spec.and(performedBefore(c.to()));

        return repository.findAll(spec, pageable).map(AuditLogMapper::toResponse);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "system";
        }
        return auth.getName();
    }
}
