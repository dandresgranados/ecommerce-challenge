package com.tgs.ecommerce.order.repository;

import com.tgs.ecommerce.order.domain.DiscountWindow;
import com.tgs.ecommerce.order.domain.DiscountWindowType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link DiscountWindow}.
 *
 * <p>La consulta {@link #findActiveAt} devuelve las ventanas de un tipo
 * concreto que están activas EN un momento dado. Se usa en el
 * {@code DiscountCalculator} al crear una orden.
 */
public interface DiscountWindowRepository extends JpaRepository<DiscountWindow, Long> {

    /**
     * Ventanas activas del tipo indicado que contienen el instante {@code at}.
     * En condiciones normales devolverá 0 o 1 elemento por tipo, pero
     * conceptualmente pueden solaparse — el caller decide qué hacer.
     */
    @Query("""
        SELECT w FROM DiscountWindow w
        WHERE w.type = :type
          AND w.active = true
          AND w.startAt <= :at
          AND w.endAt   >= :at
        """)
    List<DiscountWindow> findActiveAt(@Param("type") DiscountWindowType type,
                                      @Param("at") Instant at);
}
