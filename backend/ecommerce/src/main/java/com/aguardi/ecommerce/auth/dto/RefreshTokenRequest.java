// ============================================
// FILE: src/main/java/com/aguardi/auth/dto/RefreshTokenRequest.java
// Propósito: DTO para solicitud de refresh token
// ============================================

package com.aguardi.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
