// ============================================
// FILE: src/main/java/com/aguardi/product/repository/ProductImageRepository.java
// Propósito: Repositorio de imágenes de productos
// ============================================

package com.aguardi.ecommerce.product.repository;

import com.aguardi.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Buscar todas las imágenes de un producto
     * @param productId ID del producto
     * @return Lista de imágenes ordenadas por displayOrder
     */
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    /**
     * Buscar imagen principal de un producto
     * @param productId ID del producto
     * @return Optional con la imagen principal
     */
    Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);

    /**
     * Buscar imagen por publicId de Cloudinary
     * @param publicId Public ID de Cloudinary
     * @return Optional con la imagen
     */
    Optional<ProductImage> findByPublicId(String publicId);

    /**
     * Contar imágenes de un producto
     * @param productId ID del producto
     * @return Cantidad de imágenes
     */
    long countByProductId(Long productId);

    /**
     * Remover flag de imagen principal de todas las imágenes de un producto
     * @param productId ID del producto
     */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.id = :productId")
    void removeMainFlagFromProductImages(@Param("productId") Long productId);

    /**
     * Establecer una imagen como principal
     * @param imageId ID de la imagen
     * @param productId ID del producto (para seguridad)
     */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = true WHERE pi.id = :imageId AND pi.product.id = :productId")
    void setAsMain(@Param("imageId") Long imageId, @Param("productId") Long productId);

    /**
     * Eliminar todas las imágenes de un producto
     * @param productId ID del producto
     */
    void deleteByProductId(Long productId);
}
