// ============================================
// FILE: src/main/java/com/aguardi/payment/entity/PaymentMethod.java
// Propósito: Enum de métodos de pago
// ============================================

package com.aguardi.ecommerce.payment.entity;

public enum PaymentMethod {
    CREDIT_CARD,      // Tarjeta de crédito
    DEBIT_CARD,       // Tarjeta de débito
    BANK_TRANSFER,    // Transferencia bancaria
    MERCADOPAGO,      // MercadoPago (genérico)
    CASH,             // Efectivo
    OTHER             // Otros métodos
}
