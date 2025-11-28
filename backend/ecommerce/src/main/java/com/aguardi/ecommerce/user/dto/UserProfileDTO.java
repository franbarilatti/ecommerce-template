// ============================================
// FILE: src/main/java/com/aguardi/user/dto/UserProfileDTO.java
// Propósito: DTO para perfil completo de usuario (incluye direcciones)
// ============================================

package com.aguardi.ecommerce.user.dto;

import com.aguardi.ecommerce.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
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

    @Builder.Default
    private List<AddressDTO> addresses = new ArrayList<>();

    // Estadísticas del usuario (opcional)
    private Integer totalOrders;
    private Integer totalPurchases;
}
