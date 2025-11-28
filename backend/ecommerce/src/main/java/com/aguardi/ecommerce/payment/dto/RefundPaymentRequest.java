// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/RefundPaymentRequest.java
// Propósito: DTO para solicitar reembolso
// ============================================

package com.aguardi.ecommerce.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPaymentRequest {

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount; // null = reembolso total

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;
}