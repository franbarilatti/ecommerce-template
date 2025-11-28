// ============================================
// FILE: src/main/java/com/aguardi/product/dto/ProductDetailDTO.java
// Propósito: DTO completo de producto con todas las imágenes
// ============================================

package com.aguardi.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private CategoryDTO category;
    private Integer stock;
    private BigDecimal weight;
    private Boolean isNew;
    private Boolean onSale;
    private Boolean active;
    private String sku;

    @Builder.Default
    private List<ProductImageDTO> images = new ArrayList<>();

    private Integer discountPercentage;
    private BigDecimal effectivePrice;
    private Boolean inStock;
    private LocalDateTime createdAt;

    // Productos relacionados (opcional)
    private List<ProductDTO> relatedProducts;
}