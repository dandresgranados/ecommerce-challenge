package com.tgs.ecommerce.order.repository;

import com.tgs.ecommerce.order.domain.Order;
import com.tgs.ecommerce.order.domain.OrderStatus;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Order}.
 *
 * <p>Los métodos de contador se usan en:
 * <ul>
 *   <li>{@link com.tgs.ecommerce.order.service.DiscountCalculator} para
 *       determinar si el usuario es "cliente frecuente".</li>
 *   <li>Los reportes (Fase 3.5).</li>
 * </ul>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Órdenes del usuario, ordenadas por fecha de creación desc. */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Todas las órdenes ordenadas por fecha de creación desc (para ADMIN). */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Cuenta órdenes del usuario en un status concreto desde una fecha. */
    long countByUserIdAndStatusAndCreatedAtGreaterThanEqual(
        Long userId, OrderStatus status, Instant since);
}
