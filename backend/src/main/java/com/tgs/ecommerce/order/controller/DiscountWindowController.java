package com.tgs.ecommerce.order.controller;

import com.tgs.ecommerce.order.dto.DiscountWindowRequest;
import com.tgs.ecommerce.order.dto.DiscountWindowResponse;
import com.tgs.ecommerce.order.service.DiscountWindowService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de ventanas de descuento.
 *
 * <p>Todos los endpoints requieren rol ADMIN — gestionar ventanas es una
 * operación sensible que afecta el precio final de todas las órdenes.
 */
@RestController
@RequestMapping("/api/discount-windows")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DiscountWindowController {

    private final DiscountWindowService service;

    @GetMapping
    public ResponseEntity<List<DiscountWindowResponse>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountWindowResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<DiscountWindowResponse> create(@Valid @RequestBody DiscountWindowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountWindowResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody DiscountWindowRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
