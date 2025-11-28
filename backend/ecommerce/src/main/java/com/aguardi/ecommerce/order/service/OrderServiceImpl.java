// ============================================
// FILE: src/main/java/com/aguardi/order/service/OrderServiceImpl.java
// Propósito: Implementación del servicio de órdenes
// ============================================

package com.aguardi.ecommerce.order.service;

import com.aguardi.ecommerce.order.dto.*;
import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.order.entity.OrderItem;
import com.aguardi.ecommerce.order.entity.OrderStatus;
import com.aguardi.ecommerce.order.entity.ShippingInfo;
import com.aguardi.ecommerce.order.mapper.OrderMapper;
import com.aguardi.ecommerce.order.mapper.ShippingInfoMapper;
import com.aguardi.ecommerce.order.repository.OrderRepository;
import com.aguardi.ecommerce.product.entity.Product;
import com.aguardi.ecommerce.product.repository.ProductRepository;
import com.aguardi.ecommerce.shared.exception.BadRequestException;
import com.aguardi.ecommerce.shared.exception.ForbiddenException;
import com.aguardi.ecommerce.shared.exception.InsufficientStockException;
import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import com.aguardi.ecommerce.shared.util.SecurityUtils;
import com.aguardi.ecommerce.user.entity.User;
import com.aguardi.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ShippingInfoMapper shippingInfoMapper;

    @Value("${app.shipping.free-shipping-threshold}")
    private BigDecimal freeShippingThreshold;

    @Value("${app.shipping.default-cost}")
    private BigDecimal defaultShippingCost;

    @Override
    @Transactional
    public OrderDetailDTO createOrder(CreateOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating order for user: {}", userId);

        // Validar que hay items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("La orden debe tener al menos un producto");
        }

        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Crear orden
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerNotes(request.getCustomerNotes());
        order.setDiscount(BigDecimal.ZERO);

        // Procesar items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            // Buscar producto
            Product product = productRepository.findByIdAndActiveTrue(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto", "id", itemRequest.getProductId()
                    ));

            // Verificar stock
            if (!product.hasStock(itemRequest.getQuantity())) {
                throw new InsufficientStockException(
                        product.getId(),
                        product.getName(),
                        itemRequest.getQuantity(),
                        product.getStock()
                );
            }

            // Crear item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductPrice(product.getEffectivePrice());
            item.setQuantity(itemRequest.getQuantity());
            item.setProductImageUrl(
                    product.getMainImage() != null ? product.getMainImage().getUrl() : null
            );

            orderItems.add(item);

            // Acumular subtotal
            subtotal = subtotal.add(item.getLineTotal());

            // Reducir stock
            product.reduceStock(itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        // Crear información de envío
        ShippingInfo shippingInfo = shippingInfoMapper.toEntity(request.getShippingInfo());
        shippingInfo.setOrder(order);
        order.setShippingInfo(shippingInfo);

        // Calcular costo de envío
        BigDecimal shippingCost = calculateShippingCostInternal(subtotal);
        order.setShippingCost(shippingCost);

        // Calcular total
        order.calculateTotal();

        // Guardar orden
        order = orderRepository.save(order);

        log.info("Order created successfully: {} ({})", order.getId(), order.getOrderNumber());

        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCurrentUserOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Getting orders for user: {}", userId);

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(orderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderById(Long orderId) {
        log.info("Getting order by ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", orderId));

        // Verificar permisos (solo el dueño o admin)
        if (!SecurityUtils.isOwnerOrAdmin(order.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver esta orden");
        }

        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderByOrderNumber(String orderNumber) {
        log.info("Getting order by order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "orderNumber", orderNumber));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(order.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver esta orden");
        }

        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.info("Getting all orders");

        // Solo admins pueden ver todas las órdenes
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden ver todas las órdenes");
        }

        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.info("Getting orders by status: {}", status);

        // Solo admins
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden filtrar órdenes");
        }

        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(orderMapper::toDTO);
    }

    @Override
    @Transactional
    public OrderDetailDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status: {} to {}", orderId, request.getStatus());

        // Solo admins pueden actualizar estado
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden actualizar el estado de órdenes");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", orderId));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Validar transición de estado
        validateStatusTransition(oldStatus, newStatus);

        // Actualizar estado
        order.setStatus(newStatus);

        // Actualizar timestamps según el nuevo estado
        switch (newStatus) {
            case PAID -> order.setPaidAt(LocalDateTime.now());
            case SHIPPED -> {
                order.setShippedAt(LocalDateTime.now());
                // Actualizar tracking info
                if (request.getTrackingNumber() != null) {
                    ShippingInfo shippingInfo = order.getShippingInfo();
                    shippingInfo.setTrackingNumber(request.getTrackingNumber());
                    shippingInfo.setCarrier(request.getCarrier());
                }
            }
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                // Restaurar stock
                restoreStock(order);
            }
        }

        // Actualizar notas admin si se proporcionan
        if (request.getAdminNotes() != null) {
            order.setAdminNotes(request.getAdminNotes());
        }

        order = orderRepository.save(order);

        log.info("Order status updated: {} from {} to {}", orderId, oldStatus, newStatus);

        // TODO: Enviar email de notificación al cliente

        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", orderId));

        // Verificar permisos (solo el dueño o admin)
        if (!SecurityUtils.isOwnerOrAdmin(order.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para cancelar esta orden");
        }

        // Verificar que la orden puede ser cancelada
        if (!order.canBeCancelled()) {
            throw new BadRequestException(
                    "No se puede cancelar una orden en estado: " + order.getStatus()
            );
        }

        // Cancelar orden
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        // Restaurar stock
        restoreStock(order);

        order = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);

        // TODO: Enviar email de confirmación de cancelación

        return orderMapper.toDetailDTO(order);
    }

    @Override
    public BigDecimal calculateShippingCost(ShippingInfoDTO shippingInfo) {
        // Por ahora, costo fijo
        // En el futuro se puede calcular por provincia, peso, etc.
        return defaultShippingCost;
    }

    // ========================================
    // MÉTODOS PRIVADOS
    // ========================================

    /**
     * Calcular costo de envío basado en el subtotal
     */
    private BigDecimal calculateShippingCostInternal(BigDecimal subtotal) {
        // Envío gratis si supera el threshold
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        return defaultShippingCost;
    }

    /**
     * Validar que la transición de estado es válida
     */
    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // Definir transiciones válidas
        boolean isValid = switch (from) {
            case PENDING -> to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false; // Estados finales
        };

        if (!isValid) {
            throw new BadRequestException(
                    String.format("Transición de estado inválida: %s -> %s", from, to)
            );
        }
    }

    /**
     * Restaurar stock de los productos de una orden cancelada
     */
    private void restoreStock(Order order) {
        log.info("Restoring stock for cancelled order: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.restoreStock(item.getQuantity());
            productRepository.save(product);

            log.debug("Restored {} units of product: {}",
                    item.getQuantity(), product.getName());
        }
    }
}