// ============================================
// FILE: src/main/java/com/aguardi/order/entity/OrderStatus.java
// Propósito: Enum de estados de pedido
// ============================================

package com.aguardi.ecommerce.order.entity;

public enum OrderStatus {
    PENDING,      // Pendiente de pago
    PAID,         // Pagado, esperando procesamiento
    PROCESSING,   // En procesamiento
    SHIPPED,      // Enviado
    DELIVERED,    // Entregado
    CANCELLED,    // Cancelado
    REFUNDED      // Reembolsado
}
