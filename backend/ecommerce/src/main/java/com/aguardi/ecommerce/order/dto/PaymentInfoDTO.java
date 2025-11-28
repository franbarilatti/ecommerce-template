// ============================================
// FILE: src/main/java/com/aguardi/order/dto/PaymentInfoDTO.java
// Propósito: DTO con información básica de pago (para incluir en OrderDetailDTO)
// ============================================

package com.aguardi.ecommerce.order.dto;

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
public class PaymentInfoDTO {
    private Long paymentId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String externalPaymentId;
    private LocalDateTime approvedAt;
}