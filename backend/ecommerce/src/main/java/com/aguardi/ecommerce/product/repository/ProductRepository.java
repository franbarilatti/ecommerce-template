// ============================================
// FILE: src/main/java/com/aguardi/product/repository/ProductRepository.java
// Propósito: Repositorio de productos con queries avanzadas
// ============================================

package com.aguardi.ecommerce.product.repository;

import com.aguardi.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS
    // ========================================

    /**
     * Buscar producto por ID solo si está activo
     * @param id ID del producto
     * @return Optional con el producto si existe y está activo
     */
    Optional<Product> findByIdAndActiveTrue(Long id);

    /**
     * Buscar producto por SKU
     * @param sku SKU del producto
     * @return Optional con el producto
     */
    Optional<Product> findBySku(String sku);

    /**
     * Verificar si existe un producto con ese SKU
     * @param sku SKU a verificar
     * @return true si existe, false si no
     */
    boolean existsBySku(String sku);

    // ========================================
    // BÚSQUEDAS POR ESTADO
    // ========================================

    /**
     * Buscar productos activos con paginación
     * @param pageable Configuración de paginación
     * @return Página de productos activos
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Buscar productos inactivos
     * @param pageable Configuración de paginación
     * @return Página de productos inactivos
     */
    Page<Product> findByActiveFalse(Pageable pageable);

    /**
     * Buscar productos nuevos
     * @param pageable Configuración de paginación
     * @return Página de productos marcados como nuevos
     */
    Page<Product> findByIsNewTrueAndActiveTrue(Pageable pageable);

    /**
     * Buscar productos en oferta
     * @param pageable Configuración de paginación
     * @return Página de productos en oferta
     */
    Page<Product> findByOnSaleTrueAndActiveTrue(Pageable pageable);

    // ========================================
    // BÚSQUEDAS POR CATEGORÍA
    // ========================================

    /**
     * Buscar productos por categoría
     * @param categoryId ID de la categoría
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Buscar productos por slug de categoría
     * @param categorySlug Slug de la categoría
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    @Query("SELECT p FROM Product p WHERE p.category.slug = :categorySlug AND p.active = true")
    Page<Product> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Contar productos por categoría
     * @param categoryId ID de la categoría
     * @return Cantidad de productos activos en esa categoría
     */
    long countByCategoryIdAndActiveTrue(Long categoryId);

    // ========================================
    // BÚSQUEDAS POR NOMBRE
    // ========================================

    /**
     * Buscar productos por nombre (contiene, ignorando mayúsculas)
     * @param name Texto a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    /**
     * Buscar productos por nombre o descripción
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND p.active = true")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========================================
    // BÚSQUEDAS POR PRECIO
    // ========================================

    /**
     * Buscar productos en un rango de precio
     * @param minPrice Precio mínimo
     * @param maxPrice Precio máximo
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Buscar productos por debajo de un precio
     * @param price Precio máximo
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Product> findByPriceLessThanEqualAndActiveTrue(BigDecimal price, Pageable pageable);

    // ========================================
    // BÚSQUEDAS POR STOCK
    // ========================================

    /**
     * Buscar productos con stock disponible
     * @param pageable Configuración de paginación
     * @return Página de productos con stock > 0
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.active = true")
    Page<Product> findProductsInStock(Pageable pageable);

    /**
     * Buscar productos sin stock
     * @return Lista de productos sin stock
     */
    @Query("SELECT p FROM Product p WHERE p.stock = 0 AND p.active = true")
    List<Product> findProductsOutOfStock();

    /**
     * Buscar productos con stock bajo (menos de X unidades)
     * @param threshold Umbral de stock bajo
     * @return Lista de productos con stock bajo
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock <= :threshold AND p.active = true")
    List<Product> findProductsWithLowStock(@Param("threshold") int threshold);

    // ========================================
    // FILTROS COMBINADOS
    // ========================================

    /**
     * Buscar productos con filtros múltiples
     * @param categoryId ID de categoría (opcional)
     * @param minPrice Precio mínimo (opcional)
     * @param maxPrice Precio máximo (opcional)
     * @param isNew Solo productos nuevos (opcional)
     * @param onSale Solo productos en oferta (opcional)
     * @param pageable Configuración de paginación
     * @return Página de productos filtrados
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:isNew IS NULL OR p.isNew = :isNew) AND " +
            "(:onSale IS NULL OR p.onSale = :onSale) AND " +
            "p.active = true")
    Page<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isNew") Boolean isNew,
            @Param("onSale") Boolean onSale,
            Pageable pageable
    );

    // ========================================
    // PRODUCTOS RELACIONADOS / RECOMENDADOS
    // ========================================

    /**
     * Buscar productos similares (misma categoría, excluyendo el actual)
     * @param categoryId ID de la categoría
     * @param excludeProductId ID del producto a excluir
     * @param pageable Configuración de paginación
     * @return Lista de productos similares
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.category.id = :categoryId AND " +
            "p.id != :excludeProductId AND " +
            "p.active = true AND " +
            "p.stock > 0")
    List<Product> findSimilarProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeProductId") Long excludeProductId,
            Pageable pageable
    );

    /**
     * Buscar productos más vendidos (basado en OrderItems - requiere join)
     * @param pageable Configuración de paginación
     * @return Lista de productos más vendidos
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN OrderItem oi ON oi.product.id = p.id " +
            "WHERE p.active = true " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(oi.id) DESC")
    List<Product> findBestSellers(Pageable pageable);

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * Contar productos activos
     * @return Total de productos activos
     */
    long countByActiveTrue();

    /**
     * Contar productos nuevos
     * @return Total de productos nuevos
     */
    long countByIsNewTrueAndActiveTrue();

    /**
     * Contar productos en oferta
     * @return Total de productos en oferta
     */
    long countByOnSaleTrueAndActiveTrue();

    /**
     * Buscar productos creados recientemente
     * @param date Fecha desde la cual buscar
     * @param pageable Configuración de paginación
     * @return Página de productos nuevos
     */
    Page<Product> findByCreatedAtAfterAndActiveTrue(LocalDateTime date, Pageable pageable);

    // ========================================
    // ACTUALIZACIONES
    // ========================================

    /**
     * Actualizar stock de un producto
     * @param productId ID del producto
     * @param newStock Nuevo valor de stock
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = :newStock WHERE p.id = :productId")
    void updateStock(@Param("productId") Long productId, @Param("newStock") int newStock);

    /**
     * Reducir stock de un producto
     * @param productId ID del producto
     * @param quantity Cantidad a reducir
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Activar/desactivar producto
     * @param productId ID del producto
     * @param active Estado a establecer
     */
    @Modifying
    @Query("UPDATE Product p SET p.active = :active WHERE p.id = :productId")
    void updateProductStatus(@Param("productId") Long productId, @Param("active") Boolean active);
}
