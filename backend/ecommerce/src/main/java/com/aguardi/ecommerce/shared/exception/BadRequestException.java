// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/BadRequestException.java
// Propósito: Excepción para solicitudes inválidas (400)
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}