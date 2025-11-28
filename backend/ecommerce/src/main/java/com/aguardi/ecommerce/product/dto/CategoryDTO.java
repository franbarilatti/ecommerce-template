// ============================================
// FILE: src/main/java/com/aguardi/product/dto/CategoryDTO.java
// Propósito: DTO para categorías
// ============================================

package com.aguardi.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean active;
    private Integer displayOrder;
    private Long productCount; // Cantidad de productos activos
}