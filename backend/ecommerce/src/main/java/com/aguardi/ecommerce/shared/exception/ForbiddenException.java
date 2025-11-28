// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/ForbiddenException.java
// Propósito: Excepción para acceso prohibido (403)
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}