// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/order/controller/OrderController.java
// Propósito: REST Controller para gestión de órdenes
// Endpoints: Crear, listar, ver detalle, cancelar, actualizar estado
// ============================================

package com.aguardi.ecommerce.order.controller;

import com.aguardi.ecommerce.order.dto.*;
import com.aguardi.ecommerce.order.entity.OrderStatus;
import com.aguardi.ecommerce.order.service.OrderService;
import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import com.aguardi.ecommerce.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Endpoints para gestión de órdenes")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    // ========================================
    // ENDPOINTS PÚBLICOS/CLIENTE
    // ========================================

    /**
     * Crear nueva orden (Checkout)
     * POST /api/orders
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Crear nueva orden",
            description = "Crea una nueva orden con los productos del carrito. Reduce el stock automáticamente."
    )
    public ResponseEntity<ApiResponse<OrderDetailDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        log.info("REST request to create order");

        OrderDetailDTO order = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Orden creada exitosamente",
                        order
                ));
    }

    /**
     * Obtener órdenes del usuario autenticado
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Mis órdenes",
            description = "Obtiene todas las órdenes del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getMyOrders(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("REST request to get current user orders - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDTO> ordersPage = orderService.getCurrentUserOrders(pageable);

        PageResponse<OrderDTO> response = PageResponse.of(ordersPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Órdenes obtenidas exitosamente",
                response
        ));
    }

    /**
     * Obtener orden por ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Ver detalle de orden",
            description = "Obtiene el detalle completo de una orden por su ID"
    )
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderById(
            @Parameter(description = "ID de la orden")
            @PathVariable Long id
    ) {
        log.info("REST request to get order by ID: {}", id);

        OrderDetailDTO order = orderService.getOrderById(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Orden obtenida exitosamente",
                order
        ));
    }

    /**
     * Obtener orden por número de orden
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Ver orden por número",
            description = "Obtiene una orden usando su número único (ej: ORD-2024-00001)"
    )
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderByNumber(
            @Parameter(description = "Número de orden (ej: ORD-2024-00001)")
            @PathVariable String orderNumber
    ) {
        log.info("REST request to get order by number: {}", orderNumber);

        OrderDetailDTO order = orderService.getOrderByOrderNumber(orderNumber);

        return ResponseEntity.ok(ApiResponse.success(
                "Orden obtenida exitosamente",
                order
        ));
    }

    /**
     * Cancelar orden
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Cancelar orden",
            description = "Cancela una orden y restaura el stock de los productos"
    )
    public ResponseEntity<ApiResponse<OrderDetailDTO>> cancelOrder(
            @Parameter(description = "ID de la orden a cancelar")
            @PathVariable Long id
    ) {
        log.info("REST request to cancel order: {}", id);

        OrderDetailDTO order = orderService.cancelOrder(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Orden cancelada exitosamente",
                order
        ));
    }

    /**
     * Calcular costo de envío
     * POST /api/orders/calculate-shipping
     */
    @PostMapping("/calculate-shipping")
    @Operation(
            summary = "Calcular costo de envío",
            description = "Calcula el costo de envío basado en la dirección de destino"
    )
    public ResponseEntity<ApiResponse<BigDecimal>> calculateShipping(
            @Valid @RequestBody ShippingInfoDTO shippingInfo
    ) {
        log.info("REST request to calculate shipping cost");

        BigDecimal shippingCost = orderService.calculateShippingCost(shippingInfo);

        return ResponseEntity.ok(ApiResponse.success(
                "Costo de envío calculado",
                shippingCost
        ));
    }

    // ========================================
    // ENDPOINTS ADMIN
    // ========================================

    /**
     * Obtener todas las órdenes (Admin)
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar todas las órdenes",
            description = "Obtiene todas las órdenes del sistema (solo administradores)"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("REST request to get all orders - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<OrderDTO> ordersPage = orderService.getAllOrders(pageable);

        PageResponse<OrderDTO> response = PageResponse.of(ordersPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Órdenes obtenidas exitosamente",
                response
        ));
    }

    /**
     * Obtener órdenes por estado (Admin)
     * GET /api/orders/admin/by-status/{status}
     */
    @GetMapping("/admin/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar órdenes por estado",
            description = "Filtra órdenes por estado (PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getOrdersByStatus(
            @Parameter(description = "Estado de la orden")
            @PathVariable OrderStatus status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("REST request to get orders by status: {}", status);

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> ordersPage = orderService.getOrdersByStatus(status, pageable);

        PageResponse<OrderDTO> response = PageResponse.of(ordersPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Órdenes filtradas exitosamente",
                response
        ));
    }

    /**
     * Actualizar estado de orden (Admin)
     * PATCH /api/orders/admin/{id}/status
     */
    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Actualizar estado de orden",
            description = "Actualiza el estado de una orden (ej: marcar como enviada, entregada, etc.)"
    )
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateOrderStatus(
            @Parameter(description = "ID de la orden")
            @PathVariable Long id,

            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        log.info("REST request to update order status: {} to {}", id, request.getStatus());

        OrderDetailDTO order = orderService.updateOrderStatus(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Estado de orden actualizado exitosamente",
                order
        ));
    }

    // ========================================
    // ENDPOINT DE SALUD (Testing)
    // ========================================

    /**
     * Health check del módulo de órdenes
     * GET /api/orders/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verifica que el módulo de órdenes está funcionando"
    )
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(
                MessageResponse.success("Order module is running")
        );
    }
}