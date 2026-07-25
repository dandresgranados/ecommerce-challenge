package com.tgs.ecommerce.product.service;

import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.product.domain.Category;
import com.tgs.ecommerce.product.dto.CategoryRequest;
import com.tgs.ecommerce.product.dto.CategoryResponse;
import com.tgs.ecommerce.product.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de categorías de productos.
 *
 * <p>Reglas de negocio:
 * <ul>
 *   <li>{@code name} único (ya validado por índice, aquí devolvemos un
 *       error 409 amigable en lugar del error crudo de la BD).</li>
 *   <li>No se puede borrar una categoría con productos asociados
 *       (se validaría con {@code productRepository.existsByCategoryId} en
 *       la Fase 3.3b — de momento se aplica el {@code CASCADE} de JPA).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
            .map(CategoryMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id)
            .map(CategoryMapper::toResponse)
            .orElseThrow(() -> ResourceNotFoundException.of("Categoría", id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessRuleException("Ya existe una categoría con nombre '" + request.name() + "'");
        }
        Category c = Category.builder()
            .name(request.name())
            .description(request.description())
            .build();
        Category saved = categoryRepository.save(c);
        log.info("Categoría creada: id={} name={}", saved.getId(), saved.getName());
        return CategoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category c = categoryRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Categoría", id));

        // Si cambia el nombre, verificar que no colisione con otro registro.
        if (!c.getName().equals(request.name())
            && categoryRepository.existsByName(request.name())) {
            throw new BusinessRuleException("Ya existe una categoría con nombre '" + request.name() + "'");
        }

        c.setName(request.name());
        c.setDescription(request.description());
        log.info("Categoría actualizada id={}", id);
        return CategoryMapper.toResponse(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Categoría", id));
        // En Fase 3.3b comprobaremos que no queden productos vinculados
        // antes de permitir el borrado físico.
        categoryRepository.delete(c);
        log.info("Categoría eliminada id={}", id);
    }
}
