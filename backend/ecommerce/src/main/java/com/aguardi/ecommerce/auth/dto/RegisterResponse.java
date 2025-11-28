// ============================================
// FILE: src/main/java/com/aguardi/auth/dto/RegisterResponse.java
// Propósito: DTO para respuesta de registro exitoso
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
public class RegisterResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String message;

    // Token incluido para login automático después del registro
    private String token;
    private String tokenType;

    @Builder.Default
    private String tokenType = "Bearer";
}