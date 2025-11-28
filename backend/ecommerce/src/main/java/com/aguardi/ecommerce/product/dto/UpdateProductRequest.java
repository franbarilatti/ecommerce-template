// ============================================
// FILE: src/main/java/com/aguardi/product/dto/UpdateProductRequest.java
// Propósito: DTO para actualizar producto existente
// ============================================

package com.aguardi.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "El precio de oferta debe ser mayor a 0")
    private BigDecimal salePrice;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    private BigDecimal weight;

    private Boolean isNew;
    private Boolean onSale;
    private Boolean active;

    @Size(max = 100, message = "El SKU no puede exceder 100 caracteres")
    private String sku;
}