// ============================================
// FILE: src/main/java/com/aguardi/payment/entity/Payment.java
// Propósito: Entidad principal de Pago
// ============================================

package com.aguardi.ecommerce.payment.entity;

import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_external_id", columnList = "external_payment_id"),
        @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // IDs externos de MercadoPago
    @Column(unique = true)
    private String externalPaymentId;  // ID del pago en MercadoPago

    @Column
    private String preferenceId;  // ID de la preferencia de MercadoPago

    @Column
    private String merchantOrderId;  // ID de orden del comerciante

    // Detalles del pago
    @Column(length = 1000)
    private String paymentDetails;  // JSON con detalles adicionales

    @Column(length = 500)
    private String statusDetail;  // Detalle del estado (ej: "rejected by insufficient_amount")

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime rejectedAt;

    @Column
    private LocalDateTime refundedAt;

    // Método para verificar si el pago fue exitoso
    public boolean isSuccessful() {
        return status == PaymentStatus.APPROVED;
    }

    // Método para verificar si el pago está pendiente
    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.IN_PROCESS;
    }

    // Método para verificar si el pago fue rechazado
    public boolean isRejected() {
        return status == PaymentStatus.REJECTED || status == PaymentStatus.CANCELLED;
    }

    // Método para verificar si el pago puede ser reembolsado
    public boolean canBeRefunded() {
        return status == PaymentStatus.APPROVED && refundedAt == null;
    }
}