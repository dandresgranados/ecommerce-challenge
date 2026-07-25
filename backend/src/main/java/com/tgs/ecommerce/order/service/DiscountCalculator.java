package com.tgs.ecommerce.order.service;

import com.tgs.ecommerce.order.domain.DiscountWindowType;
import com.tgs.ecommerce.order.domain.OrderStatus;
import com.tgs.ecommerce.order.repository.DiscountWindowRepository;
import com.tgs.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Calcula el descuento aplicable a una orden combinando las tres reglas
 * de negocio del reto:
 *
 * <ul>
 *   <li><b>Ventana global (10 %)</b> — todas las órdenes creadas dentro
 *       de una {@code DiscountWindow(type=GLOBAL, active=true)} vigente.</li>
 *   <li><b>Pedido aleatorio (50 %)</b> — cuando el request marca
 *       {@code randomOrder=true} y hay una {@code DiscountWindow(type=RANDOM)}
 *       vigente en ese momento.</li>
 *   <li><b>Cliente frecuente (5 %)</b> — si el usuario tiene N o más órdenes
 *       {@code PAID} en los últimos {@code windowDays} (parámetros en
 *       {@link LoyaltyProperties}).</li>
 * </ul>
 *
 * <p>El total se acota a 0.9500 para que ninguna orden llegue a total = 0.
 *
 * <p>Bean puro (sin {@code @Transactional}) — la persistencia la maneja
 * {@code OrderService}. Se puede testear con Mockito muy fácilmente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountCalculator {

    private static final BigDecimal GLOBAL_RATE = new BigDecimal("0.1000");
    private static final BigDecimal RANDOM_RATE = new BigDecimal("0.5000");
    private static final BigDecimal MAX_TOTAL_RATE = new BigDecimal("0.9500");

    private final DiscountWindowRepository discountWindowRepository;
    private final OrderRepository orderRepository;
    private final LoyaltyProperties loyaltyProperties;

    /**
     * @param userId       id del usuario que hace la compra
     * @param randomOrder  si activó la opción "pedido aleatorio"
     * @param now          momento de referencia (inyectable para tests)
     * @return desglose de descuento (nunca null)
     */
    public DiscountBreakdown calculate(Long userId, boolean randomOrder, Instant now) {
        BigDecimal globalRate = hasActive(DiscountWindowType.GLOBAL, now)
            ? GLOBAL_RATE : BigDecimal.ZERO;

        BigDecimal randomRate = (randomOrder && hasActive(DiscountWindowType.RANDOM, now))
            ? RANDOM_RATE : BigDecimal.ZERO;

        BigDecimal loyaltyRate = isFrequentCustomer(userId, now)
            ? loyaltyProperties.getFrequentDiscountRate().setScale(4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal total = globalRate.add(randomRate).add(loyaltyRate);
        if (total.compareTo(MAX_TOTAL_RATE) > 0) {
            log.debug("Descuento acotado de {} a {}", total, MAX_TOTAL_RATE);
            total = MAX_TOTAL_RATE;
        }

        log.debug("Descuento userId={} → global={} random={} loyalty={} total={}",
            userId, globalRate, randomRate, loyaltyRate, total);
        return new DiscountBreakdown(globalRate, randomRate, loyaltyRate, total);
    }

    private boolean hasActive(DiscountWindowType type, Instant at) {
        return !discountWindowRepository.findActiveAt(type, at).isEmpty();
    }

    private boolean isFrequentCustomer(Long userId, Instant now) {
        Instant since = now.minus(loyaltyProperties.getWindowDays(), ChronoUnit.DAYS);
        long paidOrders = orderRepository.countByUserIdAndStatusAndCreatedAtGreaterThanEqual(
            userId, OrderStatus.PAID, since);
        return paidOrders >= loyaltyProperties.getFrequentCustomerThreshold();
    }
}
