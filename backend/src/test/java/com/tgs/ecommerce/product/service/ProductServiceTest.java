package com.tgs.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.service.AuditService;
import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.inventory.domain.Inventory;
import com.tgs.ecommerce.inventory.repository.InventoryRepository;
import com.tgs.ecommerce.product.domain.Category;
import com.tgs.ecommerce.product.domain.Product;
import com.tgs.ecommerce.product.dto.ProductRequest;
import com.tgs.ecommerce.product.dto.ProductResponse;
import com.tgs.ecommerce.product.repository.CategoryRepository;
import com.tgs.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios de {@link ProductService}.
 *
 * <p>Ejemplo del patrón "service + mocks": todas las dependencias (3 repos +
 * AuditService) se mockean. Verificamos tanto el <em>resultado</em>
 * ({@code assertThat}) como las <em>interacciones</em> ({@code verify}).
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock  private ProductRepository productRepository;
    @Mock  private CategoryRepository categoryRepository;
    @Mock  private InventoryRepository inventoryRepository;
    @Mock  private AuditService auditService;

    @InjectMocks
    private ProductService productService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electrónica").build();
    }

    // ------------------------------------------------------------
    // create()
    // ------------------------------------------------------------

    @Test
    @DisplayName("create → guarda producto + inventario y registra auditoría CREATE")
    void createGuardaProductoInventarioYAudita() {
        // Arrange
        ProductRequest request = new ProductRequest(
            "SKU-1", "Producto Test", "desc",
            new BigDecimal("10.00"), 1L, true, 20, 5);

        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        // Mockito devuelve el argumento tal cual (simula el save que asigna id)
        when(productRepository.save(any(Product.class)))
            .thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(100L);
                return p;
            });

        // Act
        ProductResponse response = productService.create(request);

        // Assert - resultado
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.stock()).isEqualTo(20);
        assertThat(response.active()).isTrue();

        // Assert - interacciones: inventario creado con la cantidad correcta
        ArgumentCaptor<Inventory> invCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(invCaptor.capture());
        assertThat(invCaptor.getValue().getQuantity()).isEqualTo(20);
        assertThat(invCaptor.getValue().getMinStock()).isEqualTo(5);

        // Assert - auditoría registrada con la acción correcta
        verify(auditService).log(
            eq(AuditAction.CREATE), eq("Product"), eq(100L), any(String.class));
    }

    @Test
    @DisplayName("create con SKU duplicado → BusinessRuleException y NO se guarda nada")
    void createFallaSiSkuDuplicado() {
        ProductRequest request = new ProductRequest(
            "SKU-DUP", "x", null, new BigDecimal("1"), 1L, null, 0, 0);
        when(productRepository.existsBySku("SKU-DUP")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("SKU");

        // Verify: nada se guardó ni se auditó
        verify(productRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any());
    }

    @Test
    @DisplayName("create con categoría inexistente → ResourceNotFoundException")
    void createFallaSiCategoriaInexistente() {
        ProductRequest request = new ProductRequest(
            "SKU-X", "x", null, new BigDecimal("1"), 999L, null, 0, 0);
        when(productRepository.existsBySku("SKU-X")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        verify(productRepository, never()).save(any());
    }

    // ------------------------------------------------------------
    // delete() — soft-delete
    // ------------------------------------------------------------

    @Test
    @DisplayName("delete marca active=false y registra auditoría DELETE (no borra físicamente)")
    void deleteHaceSoftDelete() {
        Product product = Product.builder()
            .id(50L).sku("SKU-DEL").name("x").active(true).build();
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));

        productService.delete(50L);

        assertThat(product.getActive()).isFalse();
        // NO se llama a delete físico (tipo explícito por la sobrecarga con
        // JpaSpecificationExecutor.delete(DeleteSpecification)).
        verify(productRepository, never()).delete(any(Product.class));
        // Sí se audita:
        verify(auditService).log(
            eq(AuditAction.DELETE), eq("Product"), eq(50L), any(String.class));
    }

    @Test
    @DisplayName("delete con id inexistente → ResourceNotFoundException")
    void deleteFallaSiProductoNoExiste() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(404L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(auditService, never()).log(any(), any(), any(), any());
    }
}
