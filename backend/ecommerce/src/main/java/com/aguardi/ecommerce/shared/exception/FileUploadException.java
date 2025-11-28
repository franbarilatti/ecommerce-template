// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/FileUploadException.java
// Propósito: Excepción para errores de upload de archivos
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}