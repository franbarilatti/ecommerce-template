// ============================================
// FILE: src/main/java/com/aguardi/product/dto/ProductFilterRequest.java
// Propósito: DTO para filtrar productos en el catálogo
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
public class ProductFilterRequest {
    private Long categoryId;
    private String categorySlug;
    private String searchTerm;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isNew;
    private Boolean onSale;
    private Boolean inStock;

    // Paginación y ordenamiento
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 12;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
