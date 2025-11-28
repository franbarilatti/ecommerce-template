// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/EmailException.java
// Propósito: Excepción para errores de envío de email
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class EmailException extends RuntimeException {

    private String recipient;

    public EmailException(String message) {
        super(message);
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailException(String message, String recipient) {
        super(message);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}