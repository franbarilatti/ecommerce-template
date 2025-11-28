// ============================================
// FILE: src/main/java/com/aguardi/payment/entity/PaymentStatus.java
// Propósito: Enum de estados de pago
// ============================================

package com.aguardi.ecommerce.payment.entity;

public enum PaymentStatus {
    PENDING,        // Pendiente de procesamiento
    IN_PROCESS,     // En proceso de revisión
    APPROVED,       // Aprobado
    REJECTED,       // Rechazado
    CANCELLED,      // Cancelado
    REFUNDED,       // Reembolsado
    CHARGED_BACK    // Contracargo
}
