// ============================================
// FILE: src/main/java/com/aguardi/user/dto/AddressDTO.java
// Propósito: DTO para direcciones de envío
// ============================================

package com.aguardi.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private Long id;
    private Long userId;
    private String street;
    private String number;
    private String floor;
    private String apartment;
    private String city;
    private String province;
    private String postalCode;
    private String reference;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    // Método helper
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(" ").append(number);

        if (floor != null && !floor.isEmpty()) {
            sb.append(", Piso ").append(floor);
        }

        if (apartment != null && !apartment.isEmpty()) {
            sb.append(", Dto. ").append(apartment);
        }

        sb.append(", ").append(city);
        sb.append(", ").append(province);
        sb.append(" (").append(postalCode).append(")");

        return sb.toString();
    }
}