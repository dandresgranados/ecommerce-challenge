package com.tgs.ecommerce.audit.controller;

import com.tgs.ecommerce.audit.dto.AuditLogResponse;
import com.tgs.ecommerce.audit.dto.AuditLogSearchCriteria;
import com.tgs.ecommerce.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de la auditoría. Solo ADMIN.
 *
 * <p>Filtros combinables como query params:
 * <pre>
 * GET /api/audit-logs                        → todos, más recientes primero
 * GET /api/audit-logs?entityType=Order       → todo lo relacionado con órdenes
 * GET /api/audit-logs?entityType=Order&entityId=42
 * GET /api/audit-logs?performedBy=admin
 * GET /api/audit-logs?action=PAY&from=2026-01-01T00:00:00Z
 * </pre>
 *
 * <p>La paginación estándar de Spring está disponible: {@code ?page=&size=&sort=...}.
 * Por defecto se ordena por {@code performedAt desc} (los más recientes primero).
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> search(
        @ModelAttribute AuditLogSearchCriteria criteria,
        @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.search(criteria, pageable));
    }
}
