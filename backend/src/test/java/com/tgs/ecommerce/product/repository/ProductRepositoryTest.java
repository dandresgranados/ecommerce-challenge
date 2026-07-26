package com.tgs.ecommerce.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tgs.ecommerce.product.domain.Category;
import com.tgs.ecommerce.product.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de slice de datos: {@code @DataJpaTest} arranca solo la capa JPA +
 * una BD H2 en memoria dedicada al test. NO carga Security, controllers ni
 * el resto de services.
 *
 * <p>Cada test se ejecuta dentro de una transacción que se hace rollback al
 * finalizar → los tests no interfieren entre sí ni ensucian la BD.
 *
 * <p>Usamos {@link TestEntityManager} para preparar datos sin depender del
 * repository que estamos testeando (aislamiento del test).
 */
@DataJpaTest
@TestPropertySource(properties = {
    // El data.sql del perfil dev intenta insertar roles/products/etc que
    // aquí no queremos: cada test prepara sus propios datos con TestEntityManager.
    "spring.sql.init.mode=never"
})
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository productRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Electrónica");
        em.persist(category);
    }

    @Test
    @DisplayName("findByActiveTrueOrderByNameAsc devuelve solo los activos ordenados por nombre")
    void findByActiveTrueOrderByNameAsc() {
        em.persist(product("SKU-A", "Zapatos", true));
        em.persist(product("SKU-B", "Auriculares", true));
        em.persist(product("SKU-C", "Mouse antiguo", false));  // inactivo
        em.persist(product("SKU-D", "Camiseta", true));
        em.flush();

        List<Product> result = productRepository.findByActiveTrueOrderByNameAsc();

        assertThat(result)
            .extracting(Product::getName)
            .containsExactly("Auriculares", "Camiseta", "Zapatos");  // orden alfabético
        assertThat(result)
            .allMatch(Product::getActive);   // ninguno inactivo
    }

    @Test
    @DisplayName("existsBySku devuelve true/false según haya coincidencia")
    void existsBySku() {
        em.persist(product("SKU-EXISTE", "x", true));
        em.flush();

        assertThat(productRepository.existsBySku("SKU-EXISTE")).isTrue();
        assertThat(productRepository.existsBySku("SKU-INEXISTENTE")).isFalse();
    }

    @Test
    @DisplayName("findBySku recupera el producto por SKU o Optional vacío")
    void findBySku() {
        em.persist(product("SKU-1", "Producto 1", true));
        em.flush();

        assertThat(productRepository.findBySku("SKU-1"))
            .isPresent()
            .get()
            .extracting(Product::getName).isEqualTo("Producto 1");

        assertThat(productRepository.findBySku("SKU-NO")).isEmpty();
    }

    private Product product(String sku, String name, boolean active) {
        return Product.builder()
            .sku(sku)
            .name(name)
            .price(new BigDecimal("10.00"))
            .active(active)
            .category(category)
            .build();
    }
}
