// ============================================
// FILE: src/main/java/com/aguardi/order/dto/ShippingInfoDTO.java
// Propósito: DTO para información de envío
// ============================================

package com.aguardi.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String street;
    private String number;
    private String floor;
    private String apartment;
    private String city;
    private String province;
    private String postalCode;
    private String reference;
    private String trackingNumber;
    private String carrier;

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