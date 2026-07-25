package com.tgs.ecommerce.product.service;

import static com.tgs.ecommerce.product.repository.ProductSpecifications.hasNameLike;
import static com.tgs.ecommerce.product.repository.ProductSpecifications.inCategory;
import static com.tgs.ecommerce.product.repository.ProductSpecifications.isActive;
import static com.tgs.ecommerce.product.repository.ProductSpecifications.priceGte;
import static com.tgs.ecommerce.product.repository.ProductSpecifications.priceLte;

import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.inventory.domain.Inventory;
import com.tgs.ecommerce.inventory.repository.InventoryRepository;
import com.tgs.ecommerce.product.domain.Category;
import com.tgs.ecommerce.product.domain.Product;
import com.tgs.ecommerce.product.dto.ProductRequest;
import com.tgs.ecommerce.product.dto.ProductResponse;
import com.tgs.ecommerce.product.dto.ProductSearchCriteria;
import com.tgs.ecommerce.product.dto.ProductUpdateRequest;
import com.tgs.ecommerce.product.repository.CategoryRepository;
import com.tgs.ecommerce.product.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD y búsqueda dinámica de productos.
 *
 * <p>La búsqueda combina {@link Specification} según los filtros no-nulos
 * en {@link ProductSearchCriteria}. Si no viene ningún filtro, devuelve
 * todos los productos paginados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    // ------------------------------------------------------------
    // Lectura
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = Specification.allOf();
        String name = criteria.normalizedName();
        if (name != null) spec = spec.and(hasNameLike(name));
        if (criteria.categoryId() != null) spec = spec.and(inCategory(criteria.categoryId()));
        if (criteria.minPrice() != null) spec = spec.and(priceGte(criteria.minPrice()));
        if (criteria.maxPrice() != null) spec = spec.and(priceLte(criteria.maxPrice()));
        if (criteria.active() != null) spec = spec.and(isActive(criteria.active()));

        Page<Product> page = productRepository.findAll(spec, pageable);
        Map<Long, Integer> stockByProduct = loadStockFor(page.getContent());

        return page.map(p -> ProductMapper.toResponse(p, stockByProduct.get(p.getId())));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Producto", id));
        Integer stock = inventoryRepository.findByProductId(id)
            .map(Inventory::getQuantity).orElse(null);
        return ProductMapper.toResponse(p, stock);
    }

    // ------------------------------------------------------------
    // Escritura
    // ------------------------------------------------------------

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessRuleException("Ya existe un producto con SKU '" + request.sku() + "'");
        }
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> ResourceNotFoundException.of("Categoría", request.categoryId()));

        Product product = Product.builder()
            .sku(request.sku())
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .active(request.active() == null ? Boolean.TRUE : request.active())
            .category(category)
            .build();
        Product saved = productRepository.save(product);

        // Crear inventario en la misma transacción.
        int initial = request.initialStock() == null ? 0 : request.initialStock();
        int min = request.minStock() == null ? 0 : request.minStock();
        Inventory inv = Inventory.builder()
            .product(saved)
            .quantity(initial)
            .minStock(min)
            .build();
        inventoryRepository.save(inv);

        log.info("Producto creado: id={} sku={} stock={}", saved.getId(), saved.getSku(), initial);
        return ProductMapper.toResponse(saved, initial);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Producto", id));

        if (request.name() != null) p.setName(request.name());
        if (request.description() != null) p.setDescription(request.description());
        if (request.price() != null) p.setPrice(request.price());
        if (request.active() != null) p.setActive(request.active());
        if (request.categoryId() != null && !request.categoryId().equals(p.getCategory().getId())) {
            Category newCat = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Categoría", request.categoryId()));
            p.setCategory(newCat);
        }

        Integer stock = inventoryRepository.findByProductId(id)
            .map(Inventory::getQuantity).orElse(null);
        log.info("Producto actualizado id={}", id);
        return ProductMapper.toResponse(p, stock);
    }

    /**
     * Soft-delete: marca el producto como inactivo. Mantiene la fila para
     * preservar la integridad de órdenes históricas que lo referencian.
     */
    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Producto", id));
        p.setActive(false);
        log.info("Producto desactivado (soft-delete) id={}", id);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /** Carga el stock de varios productos en 1 query (evita N+1). */
    private Map<Long, Integer> loadStockFor(List<Product> products) {
        if (products.isEmpty()) return Map.of();
        List<Long> ids = products.stream().map(Product::getId).toList();
        // No hay findByProductIdIn en el repo aún, así que iteramos —
        // aceptable porque el tamaño de página es pequeño (default 20).
        return ids.stream()
            .map(inventoryRepository::findByProductId)
            .flatMap(java.util.Optional::stream)
            .collect(Collectors.toMap(i -> i.getProduct().getId(), Inventory::getQuantity));
    }
}
