package com.tgs.ecommerce.report.service;

import com.tgs.ecommerce.order.repository.OrderRepository;
import com.tgs.ecommerce.product.dto.ProductResponse;
import com.tgs.ecommerce.product.repository.ProductRepository;
import com.tgs.ecommerce.product.service.ProductMapper;
import com.tgs.ecommerce.report.dto.FrequentCustomerResponse;
import com.tgs.ecommerce.report.dto.TopSellingProductResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de reportes exigidos por el reto:
 * <ul>
 *   <li>e.i)  Productos activos</li>
 *   <li>e.ii) Top N productos más vendidos</li>
 *   <li>e.iii) Top N clientes frecuentes</li>
 * </ul>
 *
 * <p>Las órdenes en estado {@code CANCELED} se excluyen de los rankings —
 * no representan una venta efectiva.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public List<ProductResponse> activeProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(ProductMapper::toResponse)
            .toList();
    }

    public List<TopSellingProductResponse> topSellingProducts(int limit) {
        return orderRepository.findTopSellingProducts(PageRequest.of(0, limit)).stream()
            .map(TopSellingProductResponse::from)
            .toList();
    }

    public List<FrequentCustomerResponse> frequentCustomers(int limit) {
        return orderRepository.findFrequentCustomers(PageRequest.of(0, limit)).stream()
            .map(FrequentCustomerResponse::from)
            .toList();
    }
}
