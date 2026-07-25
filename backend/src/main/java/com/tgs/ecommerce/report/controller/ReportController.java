package com.tgs.ecommerce.report.controller;

import com.tgs.ecommerce.product.dto.ProductResponse;
import com.tgs.ecommerce.report.dto.FrequentCustomerResponse;
import com.tgs.ecommerce.report.dto.TopSellingProductResponse;
import com.tgs.ecommerce.report.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de reportes gerenciales.
 *
 * <p>Todos requieren rol ADMIN — son datos sensibles del negocio.
 *
 * <ul>
 *   <li>{@code GET /api/reports/products/active}
 *       — productos actualmente activos.</li>
 *   <li>{@code GET /api/reports/products/top-selling?limit=5}
 *       — top N productos más vendidos.</li>
 *   <li>{@code GET /api/reports/customers/frequent?limit=5}
 *       — top N clientes por número de órdenes.</li>
 * </ul>
 *
 * <p>{@code @Validated} en la clase habilita la validación de parámetros
 * simples ({@code @Positive}, {@code @Max}) — sin él las anotaciones
 * sobre {@code @RequestParam} se ignoran.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/products/active")
    public ResponseEntity<List<ProductResponse>> activeProducts() {
        return ResponseEntity.ok(reportService.activeProducts());
    }

    @GetMapping("/products/top-selling")
    public ResponseEntity<List<TopSellingProductResponse>> topSelling(
        @RequestParam(defaultValue = "5") @Positive @Max(100) int limit
    ) {
        return ResponseEntity.ok(reportService.topSellingProducts(limit));
    }

    @GetMapping("/customers/frequent")
    public ResponseEntity<List<FrequentCustomerResponse>> frequentCustomers(
        @RequestParam(defaultValue = "5") @Positive @Max(100) int limit
    ) {
        return ResponseEntity.ok(reportService.frequentCustomers(limit));
    }
}
