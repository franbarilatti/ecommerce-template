// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/PaymentResponse.java
// Propósito: DTO de respuesta al crear un pago
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentStatus status;

    // URLs de MercadoPago
    private String initPoint; // URL para redirigir al usuario
    private String preferenceId;

    private String message;
}