package com.tgs.ecommerce.order.service;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.service.AuditService;
import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.order.domain.DiscountWindow;
import com.tgs.ecommerce.order.dto.DiscountWindowRequest;
import com.tgs.ecommerce.order.dto.DiscountWindowResponse;
import com.tgs.ecommerce.order.repository.DiscountWindowRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de {@link DiscountWindow}. Solo accesible por ADMIN
 * (autorización aplicada en el controller).
 *
 * <p>Regla de negocio validada aquí: {@code startAt} debe ser estrictamente
 * anterior a {@code endAt}. Si no, se lanza {@link BusinessRuleException}
 * y el {@code GlobalExceptionHandler} lo traduce a 409 Conflict.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountWindowService {

    /** Nombre de entidad usado en la auditoría — evita magic string. */
    private static final String ENTITY_NAME = "DiscountWindow";

    private final DiscountWindowRepository repository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<DiscountWindowResponse> listAll() {
        return repository.findAll().stream()
            .map(DiscountWindowMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DiscountWindowResponse getById(Long id) {
        return repository.findById(id)
            .map(DiscountWindowMapper::toResponse)
            .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
    }

    @Transactional
    public DiscountWindowResponse create(DiscountWindowRequest request) {
        validateRange(request);
        DiscountWindow w = DiscountWindow.builder()
            .name(request.name())
            .type(request.type())
            .rate(request.rate())
            .startAt(request.startAt())
            .endAt(request.endAt())
            .active(request.active() == null ? Boolean.TRUE : request.active())
            .build();
        DiscountWindow saved = repository.save(w);
        log.info("DiscountWindow creada: id={} type={} rate={}", saved.getId(), saved.getType(), saved.getRate());
        auditService.log(AuditAction.CREATE, ENTITY_NAME, saved.getId(),
            "name=" + saved.getName()
            + " type=" + saved.getType()
            + " rate=" + saved.getRate()
            + " active=" + saved.getActive());
        return DiscountWindowMapper.toResponse(saved);
    }

    @Transactional
    public DiscountWindowResponse update(Long id, DiscountWindowRequest request) {
        validateRange(request);
        DiscountWindow w = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        w.setName(request.name());
        w.setType(request.type());
        w.setRate(request.rate());
        w.setStartAt(request.startAt());
        w.setEndAt(request.endAt());
        if (request.active() != null) {
            w.setActive(request.active());
        }
        log.info("DiscountWindow actualizada id={}", id);
        auditService.log(AuditAction.UPDATE, ENTITY_NAME, id,
            "name=" + w.getName()
            + " type=" + w.getType()
            + " rate=" + w.getRate()
            + " active=" + w.getActive());
        return DiscountWindowMapper.toResponse(w);
    }

    @Transactional
    public void delete(Long id) {
        DiscountWindow w = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        String snapshot = "name=" + w.getName() + " type=" + w.getType();
        repository.deleteById(id);
        log.info("DiscountWindow eliminada id={}", id);
        auditService.log(AuditAction.DELETE, ENTITY_NAME, id, snapshot);
    }

    private void validateRange(DiscountWindowRequest r) {
        if (!r.startAt().isBefore(r.endAt())) {
            throw new BusinessRuleException("startAt debe ser anterior a endAt");
        }
    }
}
