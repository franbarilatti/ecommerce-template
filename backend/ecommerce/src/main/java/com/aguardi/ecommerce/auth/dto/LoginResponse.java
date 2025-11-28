// ============================================
// FILE: src/main/java/com/aguardi/auth/dto/LoginResponse.java
// Propósito: DTO para respuesta de login exitoso
// ============================================

package com.aguardi.ecommerce.auth.dto;

import com.aguardi.ecommerce.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn; // En milisegundos

    // Información del usuario
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    @Builder.Default
    private String tokenType = "Bearer";
}

