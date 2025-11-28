// ============================================
// FILE: src/main/java/com/aguardi/order/dto/UpdateOrderStatusRequest.java
// Propósito: DTO para actualizar estado de orden
// ============================================

package com.aguardi.ecommerce.order.dto;

import com.aguardi.ecommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private OrderStatus status;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String adminNotes;

    // Campos opcionales para tracking
    private String trackingNumber;
    private String carrier;
}
