// ============================================
// FILE: src/main/java/com/aguardi/user/dto/UserDTO.java
// Propósito: DTO para exponer información básica de usuario
// ============================================

package com.aguardi.ecommerce.user.dto;

import com.aguardi.ecommerce.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Método helper
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
