// ============================================
// FILE: src/main/java/com/aguardi/product/dto/CreateCategoryRequest.java
// Propósito: DTO para crear nueva categoría
// ============================================

package com.aguardi.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "El slug es obligatorio")
    @Size(min = 2, max = 100, message = "El slug debe tener entre 2 y 100 caracteres")
    private String slug;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    private String imageUrl;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Integer displayOrder = 0;
}