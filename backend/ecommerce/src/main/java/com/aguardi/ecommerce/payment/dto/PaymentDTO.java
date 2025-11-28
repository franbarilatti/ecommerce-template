// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/PaymentDTO.java
// Propósito: DTO básico de pago
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.aguardi.ecommerce.payment.entity.PaymentMethod;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String externalPaymentId;
    private String statusDetail;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}