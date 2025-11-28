// ============================================
// FILE: src/main/java/com/aguardi/product/dto/ProductDTO.java
// Propósito: DTO básico de producto para listados
// ============================================

package com.aguardi.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Long categoryId;
    private String categoryName;
    private Integer stock;
    private Boolean isNew;
    private Boolean onSale;
    private Boolean active;
    private String mainImageUrl;
    private Integer discountPercentage;

    // Precio efectivo (con descuento si aplica)
    private BigDecimal effectivePrice;
}