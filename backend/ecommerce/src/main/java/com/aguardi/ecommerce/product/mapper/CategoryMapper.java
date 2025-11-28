// ============================================
// FILE: src/main/java/com/aguardi/product/mapper/CategoryMapper.java
// Propósito: Mapper para convertir entre Category Entity y DTOs
// ============================================

package com.aguardi.ecommerce.product.mapper;

import com.aguardi.ecommerce.product.dto.CategoryDTO;
import com.aguardi.ecommerce.product.dto.CreateCategoryRequest;
import com.aguardi.ecommerce.product.dto.UpdateCategoryRequest;
import com.aguardi.ecommerce.product.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir Category Entity a CategoryDTO
     * @param category Entidad de categoría
     * @return DTO de categoría
     */
    @Mapping(target = "productCount", expression = "java(category.getActiveProductCount())")
    CategoryDTO toDTO(Category category);

    /**
     * Convertir lista de Category a lista de CategoryDTO
     * @param categories Lista de entidades
     * @return Lista de DTOs
     */
    List<CategoryDTO> toDTOList(List<Category> categories);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateCategoryRequest a Category Entity
     * @param request Request con datos de nueva categoría
     * @return Nueva entidad de categoría
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    /**
     * Actualizar Category Entity desde UpdateCategoryRequest
     * @param request Request con datos a actualizar
     * @param category Entidad existente a actualizar
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true) // El slug no se cambia después de crear
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(UpdateCategoryRequest request, @MappingTarget Category category);
}
