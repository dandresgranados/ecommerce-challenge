package com.tgs.ecommerce.order.controller;

import com.tgs.ecommerce.order.dto.CreateOrderRequest;
import com.tgs.ecommerce.order.dto.OrderResponse;
import com.tgs.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de gestión de órdenes.
 *
 * <ul>
 *   <li>{@code POST /api/orders}         — crea (autenticado)</li>
 *   <li>{@code GET  /api/orders/my}      — mis órdenes (autenticado)</li>
 *   <li>{@code GET  /api/orders/{id}}    — detalle (autenticado)</li>
 *   <li>{@code GET  /api/orders}         — todas (ADMIN)</li>
 *   <li>{@code POST /api/orders/{id}/pay}    — pagar (autenticado)</li>
 *   <li>{@code POST /api/orders/{id}/cancel} — cancelar (autenticado)</li>
 * </ul>
 *
 * <p>Nota: en un sistema real, {@code pay}/{@code cancel}/{@code getById}
 * deberían verificar que el usuario autenticado sea el dueño de la orden
 * (o ADMIN). Para el alcance de la prueba técnica se deja al servicio la
 * validación de estado y se documenta este trade-off aquí.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
        @AuthenticationPrincipal String username,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.create(username, request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> myOrders(
        @AuthenticationPrincipal String username,
        Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.listByUsername(username, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> listAll(Pageable pageable) {
        return ResponseEntity.ok(orderService.listAll(pageable));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }
}
