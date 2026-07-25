package com.tgs.ecommerce.order.service;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la regla de "cliente frecuente".
 *
 * <p>Leída del bloque {@code app.loyalty:} de {@code application.yml}:
 * <pre>
 * app:
 *   loyalty:
 *     frequent-customer-threshold: 5
 *     window-days: 30
 *     frequent-discount-rate: 0.05
 * </pre>
 *
 * <p>Se registra con {@code @EnableConfigurationProperties(LoyaltyProperties.class)}
 * en {@link com.tgs.ecommerce.security.SecurityConfig}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.loyalty")
public class LoyaltyProperties {

    /** Nº mínimo de órdenes en la ventana para considerar cliente frecuente. */
    private int frequentCustomerThreshold = 5;

    /** Días hacia atrás sobre los que se cuentan las órdenes del usuario. */
    private int windowDays = 30;

    /** Descuento adicional (0.05 = 5 %) que reciben los clientes frecuentes. */
    private BigDecimal frequentDiscountRate = new BigDecimal("0.05");
}
