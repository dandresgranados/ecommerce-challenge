package com.tgs.ecommerce.product.repository;

import com.tgs.ecommerce.product.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de {@link Product}.
 *
 * <p>Extiende {@link JpaSpecificationExecutor} para poder ejecutar
 * consultas dinámicas construidas con {@link
 * com.tgs.ecommerce.product.repository.ProductSpecifications}.
 */
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByCategoryId(Long categoryId);

    /** Productos activos ordenados alfabéticamente (para el reporte). */
    List<Product> findByActiveTrueOrderByNameAsc();
}
