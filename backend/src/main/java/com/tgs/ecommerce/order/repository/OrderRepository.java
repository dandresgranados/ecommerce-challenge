package com.tgs.ecommerce.order.repository;

import com.tgs.ecommerce.order.domain.Order;
import com.tgs.ecommerce.order.domain.OrderStatus;
import com.tgs.ecommerce.report.dto.FrequentCustomerProjection;
import com.tgs.ecommerce.report.dto.TopSellingProductProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    // ---------------------------------------------------------------
    // Reportes (Fase 3.5)
    // ---------------------------------------------------------------

    /**
     * Productos más vendidos, excluyendo órdenes canceladas.
     *
     * <p>El límite se aplica con {@code PageRequest.of(0, N)} desde el
     * servicio. Se ordenan por unidades vendidas descendente.
     */
    @Query("""
        SELECT p.id     AS productId,
               p.sku    AS sku,
               p.name   AS name,
               SUM(oi.quantity) AS totalSold
        FROM OrderItem oi
        JOIN oi.product p
        JOIN oi.order o
        WHERE o.status <> com.tgs.ecommerce.order.domain.OrderStatus.CANCELED
        GROUP BY p.id, p.sku, p.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<TopSellingProductProjection> findTopSellingProducts(Pageable pageable);

    /**
     * Clientes con más órdenes (excluyendo canceladas). Los "top" son los
     * que más compran.
     */
    @Query("""
        SELECT u.id       AS userId,
               u.username AS username,
               u.fullName AS fullName,
               u.email    AS email,
               COUNT(o)   AS orderCount,
               COALESCE(SUM(o.total), 0) AS totalSpent
        FROM Order o
        JOIN o.user u
        WHERE o.status <> com.tgs.ecommerce.order.domain.OrderStatus.CANCELED
        GROUP BY u.id, u.username, u.fullName, u.email
        ORDER BY COUNT(o) DESC, SUM(o.total) DESC
        """)
    List<FrequentCustomerProjection> findFrequentCustomers(Pageable pageable);
}
