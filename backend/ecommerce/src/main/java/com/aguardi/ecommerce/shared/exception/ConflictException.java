// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/ConflictException.java
// Propósito: Excepción para conflictos (409) - ej: email duplicado
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}