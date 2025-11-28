// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/PaymentStatusDTO.java
// Propósito: DTO para verificar estado de un pago
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
public class PaymentStatusDTO {
    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String statusDetail;
    private String externalPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private Boolean successful;
    private Boolean pending;
    private Boolean canBeRetried;
}