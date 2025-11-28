// ============================================
// FILE: src/main/java/com/aguardi/product/controller/ProductController.java
// Propósito: Controller para endpoints de productos
// ============================================

package com.aguardi.ecommerce.product.controller;

import com.aguardi.ecommerce.product.dto.*;
import com.aguardi.ecommerce.product.service.ProductService;
import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import com.aguardi.ecommerce .shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Endpoints de gestión de productos")
public class ProductController {

    private final ProductService productService;

    /**
     * Obtener todos los productos (público)
     * GET /api/products
     */
    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Obtener lista paginada de productos activos"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get all products request received - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductDTO> products = productService.getAllProducts(pageable);
        PageResponse<ProductDTO> pageResponse = PageResponse.of(products);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Buscar productos con filtros (público)
     * GET /api/products/search
     */
    @GetMapping("/search")
    @Operation(
            summary = "Buscar productos",
            description = "Buscar productos con múltiples filtros"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Search products request received - query: {}", q);

        ProductFilterRequest filter = ProductFilterRequest.builder()
                .searchTerm(q)
                .categoryId(categoryId)
                .categorySlug(categorySlug)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isNew(isNew)
                .onSale(onSale)
                .inStock(inStock)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();

        Page<ProductDTO> products = productService.searchProducts(filter);
        PageResponse<ProductDTO> pageResponse = PageResponse.of(products);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Obtener productos por categoría (público)
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Productos por categoría",
            description = "Obtener productos de una categoría específica"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get products by category request received - categoryId: {}", categoryId);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageable);
        PageResponse<ProductDTO> pageResponse = PageResponse.of(products);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Obtener productos nuevos (público)
     * GET /api/products/new
     */
    @GetMapping("/new")
    @Operation(
            summary = "Productos nuevos",
            description = "Obtener productos marcados como nuevos"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getNewProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("Get new products request received");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ProductDTO> products = productService.getNewProducts(pageable);
        PageResponse<ProductDTO> pageResponse = PageResponse.of(products);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Obtener productos en oferta (público)
     * GET /api/products/on-sale
     */
    @GetMapping("/on-sale")
    @Operation(
            summary = "Productos en oferta",
            description = "Obtener productos con descuento"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("Get products on sale request received");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ProductDTO> products = productService.getProductsOnSale(pageable);
        PageResponse<ProductDTO> pageResponse = PageResponse.of(products);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Obtener productos más vendidos (público)
     * GET /api/products/best-sellers
     */
    @GetMapping("/best-sellers")
    @Operation(
            summary = "Productos más vendidos",
            description = "Obtener lista de productos más vendidos"
    )
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Get best sellers request received - limit: {}", limit);

        List<ProductDTO> products = productService.getBestSellers(limit);

        return ResponseEntity.ok(
                ApiResponse.success(products)
        );
    }

    /**
     * Obtener producto por ID (público)
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener producto",
            description = "Obtener detalle completo de un producto"
    )
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductById(
            @PathVariable Long id) {

        log.info("Get product by ID request received - id: {}", id);

        ProductDetailDTO product = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.success(product)
        );
    }

    /**
     * Obtener productos relacionados (público)
     * GET /api/products/{id}/related
     */
    @GetMapping("/{id}/related")
    @Operation(
            summary = "Productos relacionados",
            description = "Obtener productos similares o de la misma categoría"
    )
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit) {

        log.info("Get related products request received - productId: {}", id);

        List<ProductDTO> products = productService.getRelatedProducts(id, limit);

        return ResponseEntity.ok(
                ApiResponse.success(products)
        );
    }

    /**
     * Crear producto (solo admin)
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Crear producto",
            description = "Crear un nuevo producto (solo admin)"
    )
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("Create product request received - name: {}", request.getName());

        ProductDTO product = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Producto creado exitosamente", product));
    }

    /**
     * Actualizar producto (solo admin)
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualizar un producto existente (solo admin)"
    )
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("Update product request received - id: {}", id);

        ProductDTO product = productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Producto actualizado exitosamente", product)
        );
    }

    /**
     * Actualizar stock (solo admin)
     * PATCH /api/products/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Actualizar stock",
            description = "Actualizar stock de un producto (solo admin)"
    )
    public ResponseEntity<ApiResponse<ProductDTO>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {

        log.info("Update stock request received - productId: {}, newStock: {}", id, stock);

        ProductDTO product = productService.updateStock(id, stock);

        return ResponseEntity.ok(
                ApiResponse.success("Stock actualizado exitosamente", product)
        );
    }

    /**
     * Eliminar producto (solo admin)
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Eliminar producto",
            description = "Eliminar (desactivar) un producto (solo admin)"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> deleteProduct(
            @PathVariable Long id) {

        log.info("Delete product request received - id: {}", id);

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Producto eliminado exitosamente",
                        MessageResponse.success("Producto eliminado exitosamente")
                )
        );
    }

    /**
     * Verificar disponibilidad de stock (público)
     * GET /api/products/{id}/check-stock
     */
    @GetMapping("/{id}/check-stock")
    @Operation(
            summary = "Verificar stock",
            description = "Verificar si hay stock disponible para una cantidad"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        log.info("Check stock request received - productId: {}, quantity: {}", id, quantity);

        boolean available = productService.checkStock(id, quantity);

        String message = available
                ? "Stock disponible"
                : "Stock insuficiente";

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }
}