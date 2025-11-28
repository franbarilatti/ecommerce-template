// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/UnauthorizedException.java
// Propósito: Excepción para acceso no autorizado (401)
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}