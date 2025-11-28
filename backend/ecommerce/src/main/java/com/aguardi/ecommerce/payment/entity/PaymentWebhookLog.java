// ============================================
// FILE: src/main/java/com/aguardi/payment/entity/PaymentWebhookLog.java
// Propósito: Log de webhooks de MercadoPago (para debugging)
// ============================================

package com.aguardi.ecommerce.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_webhook_logs", indexes = {
        @Index(name = "idx_webhook_payment_id", columnList = "external_payment_id"),
        @Index(name = "idx_webhook_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String externalPaymentId;

    @Column(nullable = false)
    private String action;  // payment.created, payment.updated, etc.

    @Column(nullable = false, length = 5000)
    private String payload;  // JSON completo del webhook

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @Column(length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;
}