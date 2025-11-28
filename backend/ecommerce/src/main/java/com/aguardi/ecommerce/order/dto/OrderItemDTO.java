// ============================================
// FILE: src/main/java/com/aguardi/order/dto/OrderItemDTO.java
// Propósito: DTO para item de orden
// ============================================

package com.aguardi.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private String productImageUrl;
}