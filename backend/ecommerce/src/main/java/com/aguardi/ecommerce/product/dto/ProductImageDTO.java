// ============================================
// FILE: src/main/java/com/aguardi/product/dto/ProductImageDTO.java
// Propósito: DTO para imágenes de productos
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
public class ProductImageDTO {
    private Long id;
    private String url;
    private String altText;
    private Boolean isMain;
    private Integer displayOrder;
}