// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/PaymentRequest.java
// Propósito: DTO para crear un pago (iniciar proceso)
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.aguardi.ecommerce.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;

    // Información adicional para el pago
    private String description;
}