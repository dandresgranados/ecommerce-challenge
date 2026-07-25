package com.tgs.ecommerce.order.service;

import java.time.Year;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Genera identificadores legibles para las órdenes.
 *
 * <p>Formato: {@code ORD-<YEAR>-<8_HEX>}. Ejemplo: {@code ORD-2026-A3F91C22}.
 *
 * <p>La parte hex viene de un UUID: la probabilidad de colisión en el
 * contexto de una prueba técnica es prácticamente cero. En un sistema
 * real de alto volumen se usaría una secuencia de BD.
 */
@Component
public class OrderNumberGenerator {

    public String next() {
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + Year.now().getValue() + "-" + hex;
    }
}
