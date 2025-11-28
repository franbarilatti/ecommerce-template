// ============================================
// FILE: src/main/java/com/aguardi/order/service/OrderService.java
// Propósito: Interface del servicio de órdenes
// ============================================

package com.aguardi.ecommerce.order.service;

import com.aguardi.ecommerce.order.dto.*;
import com.aguardi.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Crear nueva orden (checkout)
     * @param request Datos de la orden
     * @return Orden creada
     */
    OrderDetailDTO createOrder(CreateOrderRequest request);

    /**
     * Obtener órdenes del usuario autenticado
     * @param pageable Paginación
     * @return Página de órdenes
     */
    Page<OrderDTO> getCurrentUserOrders(Pageable pageable);

    /**
     * Obtener detalle de orden
     * @param orderId ID de la orden
     * @return Detalle completo de la orden
     */
    OrderDetailDTO getOrderById(Long orderId);

    /**
     * Obtener orden por número de orden
     * @param orderNumber Número de orden
     * @return Detalle completo de la orden
     */
    OrderDetailDTO getOrderByOrderNumber(String orderNumber);

    /**
     * Obtener todas las órdenes (solo admin)
     * @param pageable Paginación
     * @return Página de órdenes
     */
    Page<OrderDTO> getAllOrders(Pageable pageable);

    /**
     * Obtener órdenes por estado (solo admin)
     * @param status Estado de la orden
     * @param pageable Paginación
     * @return Página de órdenes
     */
    Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);

    /**
     * Actualizar estado de orden (solo admin)
     * @param orderId ID de la orden
     * @param request Nuevo estado y datos opcionales
     * @return Orden actualizada
     */
    OrderDetailDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    /**
     * Cancelar orden
     * @param orderId ID de la orden
     * @return Orden cancelada
     */
    OrderDetailDTO cancelOrder(Long orderId);

    /**
     * Calcular costo de envío
     * @param shippingInfo Información de envío
     * @return Costo de envío calculado
     */
    java.math.BigDecimal calculateShippingCost(ShippingInfoDTO shippingInfo);
}