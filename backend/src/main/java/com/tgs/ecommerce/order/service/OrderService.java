package com.tgs.ecommerce.order.service;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.service.AuditService;
import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.inventory.domain.Inventory;
import com.tgs.ecommerce.inventory.repository.InventoryRepository;
import com.tgs.ecommerce.order.domain.Order;
import com.tgs.ecommerce.order.domain.OrderItem;
import com.tgs.ecommerce.order.domain.OrderStatus;
import com.tgs.ecommerce.order.dto.CreateOrderRequest;
import com.tgs.ecommerce.order.dto.OrderLineRequest;
import com.tgs.ecommerce.order.dto.OrderResponse;
import com.tgs.ecommerce.order.repository.OrderRepository;
import com.tgs.ecommerce.product.domain.Product;
import com.tgs.ecommerce.product.repository.ProductRepository;
import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la creación y gestión de órdenes.
 *
 * <p>El método {@link #create} es una transacción atómica que:
 * <ol>
 *   <li>Carga los productos referenciados (1 sola query in-clause).</li>
 *   <li>Carga los inventarios de esos productos.</li>
 *   <li>Valida existencia, estado activo y stock suficiente.</li>
 *   <li>Calcula subtotal (suma de {@code unitPrice × quantity}).</li>
 *   <li>Delega el cálculo de descuento al {@link DiscountCalculator}.</li>
 *   <li>Persiste la orden y sus líneas (con snapshot de nombre y precio).</li>
 *   <li>Decrementa el inventario de cada producto.</li>
 * </ol>
 * Si algún paso lanza excepción, se hace rollback total.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int MONEY_SCALE = 4;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final DiscountCalculator discountCalculator;
    private final OrderNumberGenerator orderNumberGenerator;
    private final AuditService auditService;

    // ------------------------------------------------------------
    // Creación
    // ------------------------------------------------------------

    @Transactional
    public OrderResponse create(String username, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario '" + username + "' no encontrado"));

        Instant now = Instant.now();

        // 1. Cargar productos e inventarios necesarios.
        Set<Long> productIds = request.items().stream()
            .map(OrderLineRequest::productId)
            .collect(Collectors.toSet());
        Map<Long, Product> productsById = productRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Inventory> inventoryByProductId = new HashMap<>();
        for (Long pid : productIds) {
            inventoryByProductId.put(pid, inventoryRepository.findByProductId(pid)
                .orElseThrow(() -> new BusinessRuleException(
                    "Producto " + pid + " no tiene inventario registrado")));
        }

        // 2. Validar y calcular subtotal.
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLineRequest line : request.items()) {
            Product product = productsById.get(line.productId());
            if (product == null) {
                throw ResourceNotFoundException.of("Producto", line.productId());
            }
            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessRuleException(
                    "El producto '" + product.getSku() + "' está inactivo");
            }
            Inventory inv = inventoryByProductId.get(product.getId());
            if (inv.getQuantity() < line.quantity()) {
                throw new BusinessRuleException(
                    "Stock insuficiente para '" + product.getSku()
                    + "'. Disponible: " + inv.getQuantity()
                    + ", solicitado: " + line.quantity());
            }
            BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(line.quantity()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);
        }
        subtotal = subtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        // 3. Calcular descuento (regla de negocio aislada).
        DiscountBreakdown breakdown = discountCalculator.calculate(
            user.getId(), request.isRandomOrder(), now);

        // 4. Total = subtotal * (1 - discountRate).
        BigDecimal total = subtotal
            .multiply(BigDecimal.ONE.subtract(breakdown.totalRate()))
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        // 5. Construir Order + OrderItems (snapshot).
        Order order = Order.builder()
            .orderNumber(orderNumberGenerator.next())
            .user(user)
            .subtotal(subtotal)
            .discountRate(breakdown.totalRate())
            .total(total)
            .randomOrder(request.isRandomOrder())
            .status(OrderStatus.CREATED)
            .build();

        for (OrderLineRequest line : request.items()) {
            Product product = productsById.get(line.productId());
            BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(line.quantity()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            OrderItem item = OrderItem.builder()
                .product(product)
                .productName(product.getName())     // snapshot
                .unitPrice(product.getPrice())      // snapshot
                .quantity(line.quantity())
                .lineTotal(lineTotal)
                .build();
            order.addItem(item);   // mantiene bidireccional
        }

        // 6. Persistir (cascade ALL guarda los items automáticamente).
        Order saved = orderRepository.save(order);

        // 7. Decrementar inventario (dirty checking → UPDATE al commit).
        for (OrderLineRequest line : request.items()) {
            Inventory inv = inventoryByProductId.get(line.productId());
            inv.setQuantity(inv.getQuantity() - line.quantity());
        }

        log.info("Orden creada: number={} user={} subtotal={} discountRate={} total={}",
            saved.getOrderNumber(), user.getUsername(), subtotal,
            breakdown.totalRate(), total);
        auditService.log(AuditAction.CREATE, "Order", saved.getId(),
            "orderNumber=" + saved.getOrderNumber()
            + " subtotal=" + subtotal
            + " discountRate=" + breakdown.totalRate()
            + " total=" + total
            + " randomOrder=" + request.isRandomOrder());

        return OrderMapper.toResponse(saved, breakdown);
    }

    // ------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Orden", id));
        return OrderMapper.toResponse(order, null);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listByUsername(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario '" + username + "' no encontrado"));
        return orderRepository
            .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
            .map(o -> OrderMapper.toResponse(o, null));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAll(Pageable pageable) {
        return orderRepository
            .findAllByOrderByCreatedAtDesc(pageable)
            .map(o -> OrderMapper.toResponse(o, null));
    }

    // ------------------------------------------------------------
    // Cambios de estado
    // ------------------------------------------------------------

    @Transactional
    public OrderResponse pay(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Orden", id));
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                "Solo se pueden pagar órdenes en estado CREATED (actual: "
                + order.getStatus() + ")");
        }
        order.setStatus(OrderStatus.PAID);
        log.info("Orden pagada: {}", order.getOrderNumber());
        auditService.log(AuditAction.PAY, "Order", order.getId(),
            "orderNumber=" + order.getOrderNumber() + " total=" + order.getTotal());
        return OrderMapper.toResponse(order, null);
    }

    /**
     * Cancela una orden en estado CREATED y devuelve el stock al inventario.
     * Órdenes ya PAID no se pueden cancelar por esta vía (requerirían un
     * flujo de reembolso, fuera del alcance del reto).
     */
    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Orden", id));
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                "Solo se pueden cancelar órdenes en estado CREATED (actual: "
                + order.getStatus() + ")");
        }

        // Devolver stock — cada línea suma su cantidad al inventario del producto.
        List<OrderItem> items = order.getItems();
        for (OrderItem item : items) {
            Long productId = item.getProduct().getId();
            Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessRuleException(
                    "Inventario no encontrado para producto " + productId));
            inv.setQuantity(inv.getQuantity() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELED);
        log.info("Orden cancelada y stock devuelto: {}", order.getOrderNumber());
        auditService.log(AuditAction.CANCEL, "Order", order.getId(),
            "orderNumber=" + order.getOrderNumber() + " itemsRestored=" + items.size());
        return OrderMapper.toResponse(order, null);
    }
}
