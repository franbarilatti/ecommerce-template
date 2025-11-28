// ============================================
// FILE: src/main/java/com/aguardi/product/repository/CategoryRepository.java
// Propósito: Repositorio de categorías
// ============================================

package com.aguardi.ecommerce.product.repository;

import com.aguardi.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Buscar categoría por slug
     * @param slug Slug de la categoría
     * @return Optional con la categoría
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Buscar categoría por nombre
     * @param name Nombre de la categoría
     * @return Optional con la categoría
     */
    Optional<Category> findByName(String name);

    /**
     * Verificar si existe una categoría con ese slug
     * @param slug Slug a verificar
     * @return true si existe, false si no
     */
    boolean existsBySlug(String slug);

    /**
     * Buscar categorías activas ordenadas por displayOrder
     * @return Lista de categorías activas
     */
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Buscar todas las categorías ordenadas por displayOrder
     * @return Lista de categorías
     */
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * Contar categorías activas
     * @return Total de categorías activas
     */
    long countByActiveTrue();

    /**
     * Buscar categorías con productos
     * @return Lista de categorías que tienen al menos un producto activo
     */
    @Query("SELECT DISTINCT c FROM Category c " +
            "JOIN c.products p " +
            "WHERE c.active = true AND p.active = true " +
            "ORDER BY c.displayOrder ASC")
    List<Category> findCategoriesWithProducts();
}