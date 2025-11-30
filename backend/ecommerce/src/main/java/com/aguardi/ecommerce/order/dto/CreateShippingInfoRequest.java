// ============================================
// FILE: src/main/java/com/aguardi/order/dto/CreateShippingInfoRequest.java
// Propósito: DTO para crear información de envío
// ============================================

package com.aguardi.ecommerce.order.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShippingInfoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Teléfono inválido")
    private String phone;

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 100, message = "La calle no puede exceder 100 caracteres")
    private String street;

    @NotBlank(message = "El número es obligatorio")
    @Size(max = 10, message = "El número no puede exceder 10 caracteres")
    private String number;

    @Size(max = 10, message = "El piso no puede exceder 10 caracteres")
    private String floor;

    @Size(max = 10, message = "El departamento no puede exceder 10 caracteres")
    private String apartment;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100, message = "La provincia no puede exceder 100 caracteres")
    private String province;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^[0-9]{4}$", message = "El código postal debe tener 4 dígitos")
    private String postalCode;

    @Size(max = 500, message = "La referencia no puede exceder 500 caracteres")
    private String reference;
}