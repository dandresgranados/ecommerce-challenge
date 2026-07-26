package com.tgs.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios de {@link OrderNumberGenerator}.
 * Sin mocks — la clase no depende de nada externo.
 */
class OrderNumberGeneratorTest {

    private final OrderNumberGenerator generator = new OrderNumberGenerator();

    @Test
    @DisplayName("el formato debe ser ORD-<year>-<8 hex uppercase>")
    void formatoValido() {
        String number = generator.next();

        // ORD-YYYY-XXXXXXXX (8 hex mayúsculas)
        assertThat(number).matches("^ORD-\\d{4}-[0-9A-F]{8}$");
    }

    @Test
    @DisplayName("el año generado es el año actual")
    void incluyeElAnioActual() {
        String number = generator.next();
        int currentYear = Year.now().getValue();

        assertThat(number).startsWith("ORD-" + currentYear + "-");
    }

    @Test
    @DisplayName("100 generaciones consecutivas producen 100 números distintos")
    void generaNumerosUnicos() {
        Set<String> generated = new HashSet<>();
        IntStream.range(0, 100).forEach(i -> generated.add(generator.next()));

        assertThat(generated).hasSize(100);
    }
}
