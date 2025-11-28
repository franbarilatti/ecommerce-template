// ============================================
// FILE: src/main/java/com/aguardi/order/dto/OrderDetailDTO.java
// Propósito: DTO completo de orden con items y envío
// ============================================

package com.aguardi.ecommerce.order.dto;

import com.aguardi.ecommerce.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private OrderStatus status;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    private ShippingInfoDTO shippingInfo;

    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal discount;
    private BigDecimal total;

    private String customerNotes;
    private String adminNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    // Información de pago (opcional)
    private PaymentInfoDTO paymentInfo;
}