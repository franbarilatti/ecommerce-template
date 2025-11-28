// ============================================
// FILE: src/main/java/com/aguardi/product/service/ProductService.java
// Propósito: Interface del servicio de productos
// ============================================

package com.aguardi.ecommerce.product.service;

import com.aguardi.ecommerce.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    /**
     * Obtener todos los productos activos con paginación
     * @param pageable Paginación
     * @return Página de productos
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * Buscar productos con filtros
     * @param filter Filtros de búsqueda
     * @return Página de productos filtrados
     */
    Page<ProductDTO> searchProducts(ProductFilterRequest filter);

    /**
     * Obtener productos por categoría
     * @param categoryId ID de la categoría
     * @param pageable Paginación
     * @return Página de productos
     */
    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    /**
     * Obtener productos por slug de categoría
     * @param categorySlug Slug de la categoría
     * @param pageable Paginación
     * @return Página de productos
     */
    Page<ProductDTO> getProductsByCategorySlug(String categorySlug, Pageable pageable);

    /**
     * Obtener productos nuevos
     * @param pageable Paginación
     * @return Página de productos nuevos
     */
    Page<ProductDTO> getNewProducts(Pageable pageable);

    /**
     * Obtener productos en oferta
     * @param pageable Paginación
     * @return Página de productos en oferta
     */
    Page<ProductDTO> getProductsOnSale(Pageable pageable);

    /**
     * Obtener producto por ID (detalle completo)
     * @param productId ID del producto
     * @return Producto con detalles
     */
    ProductDetailDTO getProductById(Long productId);

    /**
     * Obtener productos relacionados
     * @param productId ID del producto
     * @param limit Cantidad de productos a retornar
     * @return Lista de productos relacionados
     */
    List<ProductDTO> getRelatedProducts(Long productId, int limit);

    /**
     * Obtener productos más vendidos
     * @param limit Cantidad de productos
     * @return Lista de productos más vendidos
     */
    List<ProductDTO> getBestSellers(int limit);

    /**
     * Crear nuevo producto (solo admin)
     * @param request Datos del producto
     * @return Producto creado
     */
    ProductDTO createProduct(CreateProductRequest request);

    /**
     * Actualizar producto (solo admin)
     * @param productId ID del producto
     * @param request Datos a actualizar
     * @return Producto actualizado
     */
    ProductDTO updateProduct(Long productId, UpdateProductRequest request);

    /**
     * Actualizar stock de producto (solo admin)
     * @param productId ID del producto
     * @param newStock Nuevo stock
     * @return Producto actualizado
     */
    ProductDTO updateStock(Long productId, Integer newStock);

    /**
     * Eliminar producto (solo admin)
     * @param productId ID del producto
     */
    void deleteProduct(Long productId);

    /**
     * Verificar disponibilidad de stock
     * @param productId ID del producto
     * @param quantity Cantidad solicitada
     * @return true si hay stock suficiente
     */
    boolean checkStock(Long productId, Integer quantity);
}
