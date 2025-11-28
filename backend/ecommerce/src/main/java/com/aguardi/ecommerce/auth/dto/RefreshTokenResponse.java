// ============================================
// FILE: src/main/java/com/aguardi/auth/dto/RefreshTokenResponse.java
// Propósito: DTO para respuesta de refresh token
// ============================================

package com.aguardi.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;

    @Builder.Default
    private String tokenType = "Bearer";
}
