// ============================================
// FILE: src/main/java/com/aguardi/order/dto/CreateOrderRequest.java
// Propósito: DTO para crear nueva orden (checkout)
// ============================================

package com.aguardi.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotEmpty(message = "La orden debe tener al menos un item")
    @Valid
    private List<CreateOrderItemRequest> items;

    @NotNull(message = "La información de envío es obligatoria")
    @Valid
    private CreateShippingInfoRequest shippingInfo;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String customerNotes;
}
