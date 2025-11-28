// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/PaymentException.java
// Propósito: Excepción para errores de pago
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class PaymentException extends RuntimeException {

    private String paymentId;
    private String statusDetail;

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentException(String message, String paymentId, String statusDetail) {
        super(message);
        this.paymentId = paymentId;
        this.statusDetail = statusDetail;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatusDetail() {
        return statusDetail;
    }
}