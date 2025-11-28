// ============================================
// FILE: src/main/java/com/aguardi/product/mapper/ProductMapper.java
// Propósito: Mapper para convertir entre Product Entity y DTOs
// ============================================

package com.aguardi.ecommerce.product.mapper;

import com.aguardi.ecommerce.product.dto.*;
import com.aguardi.ecommerce.product.entity.Product;
import com.aguardi.ecommerce.product.entity.ProductImage;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class, ProductImageMapper.class}
)
public interface ProductMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir Product Entity a ProductDTO (versión básica para listados)
     * @param product Entidad de producto
     * @return DTO básico de producto
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "mainImageUrl", expression = "java(getMainImageUrl(product))")
    @Mapping(target = "discountPercentage", expression = "java(product.getDiscountPercentage())")
    @Mapping(target = "effectivePrice", expression = "java(product.getEffectivePrice())")
    ProductDTO toDTO(Product product);

    /**
     * Convertir lista de Product a lista de ProductDTO
     * @param products Lista de entidades
     * @return Lista de DTOs
     */
    List<ProductDTO> toDTOList(List<Product> products);

    /**
     * Convertir Product Entity a ProductDetailDTO (versión completa)
     * @param product Entidad de producto
     * @return DTO completo de producto
     */
    @Mapping(target = "category", source = "category")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "discountPercentage", expression = "java(product.getDiscountPercentage())")
    @Mapping(target = "effectivePrice", expression = "java(product.getEffectivePrice())")
    @Mapping(target = "inStock", expression = "java(product.getStock() > 0)")
    @Mapping(target = "relatedProducts", ignore = true) // Se setea después en el servicio
    ProductDetailDTO toDetailDTO(Product product);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateProductRequest a Product Entity
     * @param request Request con datos de nuevo producto
     * @return Nueva entidad de producto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // Se setea en el servicio
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    Product toEntity(CreateProductRequest request);

    /**
     * Actualizar Product Entity desde UpdateProductRequest
     * @param request Request con datos a actualizar
     * @param product Entidad existente a actualizar
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // Se actualiza en el servicio
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    void updateEntityFromDTO(UpdateProductRequest request, @MappingTarget Product product);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Obtener URL de imagen principal
     */
    default String getMainImageUrl(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        ProductImage mainImage = product.getMainImage();
        return mainImage != null ? mainImage.getUrl() : null;
    }

    /**
     * Enriquecer DTO con campos calculados
     */
    @AfterMapping
    default void enrichProductDTO(@MappingTarget ProductDTO dto, Product product) {
        // Ya se calculan en los @Mapping expressions
    }
}