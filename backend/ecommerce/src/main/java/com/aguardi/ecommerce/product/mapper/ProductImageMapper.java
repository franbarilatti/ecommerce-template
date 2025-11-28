// ============================================
// FILE: src/main/java/com/aguardi/product/mapper/ProductImageMapper.java
// Propósito: Mapper para convertir entre ProductImage Entity y DTOs
// ============================================

package com.aguardi.ecommerce.product.mapper;

import com.aguardi.ecommerce.product.dto.ProductImageDTO;
import com.aguardi.ecommerce.product.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductImageMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir ProductImage Entity a ProductImageDTO
     * @param image Entidad de imagen
     * @return DTO de imagen
     */
    ProductImageDTO toDTO(ProductImage image);

    /**
     * Convertir lista de ProductImage a lista de ProductImageDTO
     * @param images Lista de entidades
     * @return Lista de DTOs
     */
    List<ProductImageDTO> toDTOList(List<ProductImage> images);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir ProductImageDTO a ProductImage Entity
     * @param dto DTO de imagen
     * @return Entidad de imagen
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductImage toEntity(ProductImageDTO dto);
}