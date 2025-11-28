// ============================================
// FILE: src/main/java/com/aguardi/auth/dto/ForgotPasswordRequest.java
// Propósito: DTO para solicitud de recuperación de contraseña
// ============================================

package com.aguardi.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;
}