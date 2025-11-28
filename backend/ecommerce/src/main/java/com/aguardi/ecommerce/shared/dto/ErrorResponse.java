// ============================================
// FILE: src/main/java/com/aguardi/shared/dto/ErrorResponse.java
// Propósito: DTO para respuestas de error
// ============================================

package com.aguardi.ecommerce.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private Boolean success = false;

    private String message;

    private String error;

    private Integer status;

    private String path;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Para errores de validación
    private List<ValidationError> validationErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}