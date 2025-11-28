// ============================================
// FILE: src/main/java/com/aguardi/product/service/CategoryService.java
// Propósito: Interface del servicio de categorías
// ============================================

package com.aguardi.ecommerce.product.service;

import com.aguardi.ecommerce.product.dto.CategoryDTO;
import com.aguardi.ecommerce.product.dto.CreateCategoryRequest;
import com.aguardi.ecommerce.product.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    /**
     * Obtener todas las categorías activas
     * @return Lista de categorías activas ordenadas
     */
    List<CategoryDTO> getAllActiveCategories();

    /**
     * Obtener todas las categorías (incluye inactivas - solo admin)
     * @return Lista de todas las categorías
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Obtener categorías con productos
     * @return Lista de categorías que tienen productos activos
     */
    List<CategoryDTO> getCategoriesWithProducts();

    /**
     * Obtener categoría por ID
     * @param categoryId ID de la categoría
     * @return DTO de la categoría
     */
    CategoryDTO getCategoryById(Long categoryId);

    /**
     * Obtener categoría por slug
     * @param slug Slug de la categoría
     * @return DTO de la categoría
     */
    CategoryDTO getCategoryBySlug(String slug);

    /**
     * Crear nueva categoría (solo admin)
     * @param request Datos de la categoría
     * @return Categoría creada
     */
    CategoryDTO createCategory(CreateCategoryRequest request);

    /**
     * Actualizar categoría (solo admin)
     * @param categoryId ID de la categoría
     * @param request Datos a actualizar
     * @return Categoría actualizada
     */
    CategoryDTO updateCategory(Long categoryId, UpdateCategoryRequest request);

    /**
     * Eliminar categoría (solo admin)
     * @param categoryId ID de la categoría
     */
    void deleteCategory(Long categoryId);
}