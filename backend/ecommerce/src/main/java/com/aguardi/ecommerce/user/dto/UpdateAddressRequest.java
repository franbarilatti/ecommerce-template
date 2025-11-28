// ============================================
// FILE: src/main/java/com/aguardi/user/dto/UpdateAddressRequest.java
// Propósito: DTO para actualizar dirección existente
// ============================================

package com.aguardi.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAddressRequest {

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

    @Size(max = 200, message = "La referencia no puede exceder 200 caracteres")
    private String reference;

    private Boolean isDefault;
}