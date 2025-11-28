// ============================================
// FILE: src/main/java/com/aguardi/product/controller/CategoryController.java
// Propósito: Controller para endpoints de categorías
// ============================================

package com.aguardi.ecommerce.product.controller;

import com.aguardi.ecommerce.product.dto.CategoryDTO;
import com.aguardi.ecommerce.product.dto.CreateCategoryRequest;
import com.aguardi.ecommerce.product.dto.UpdateCategoryRequest;
import com.aguardi.ecommerce.product.service.CategoryService;
import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Endpoints de gestión de categorías")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Obtener todas las categorías activas (público)
     * GET /api/categories
     */
    @GetMapping
    @Operation(
            summary = "Listar categorías activas",
            description = "Obtener todas las categorías activas ordenadas"
    )
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllActiveCategories() {
        log.info("Get all active categories request received");

        List<CategoryDTO> categories = categoryService.getAllActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success(categories)
        );
    }

    /**
     * Obtener categorías con productos (público)
     * GET /api/categories/with-products
     */
    @GetMapping("/with-products")
    @Operation(
            summary = "Listar categorías con productos",
            description = "Obtener categorías que tienen productos activos"
    )
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoriesWithProducts() {
        log.info("Get categories with products request received");

        List<CategoryDTO> categories = categoryService.getCategoriesWithProducts();

        return ResponseEntity.ok(
                ApiResponse.success(categories)
        );
    }

    /**
     * Obtener todas las categorías incluyendo inactivas (solo admin)
     * GET /api/categories/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Listar todas las categorías",
            description = "Obtener todas las categorías incluyendo inactivas (solo admin)"
    )
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        log.info("Get all categories request received");

        List<CategoryDTO> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success(categories)
        );
    }

    /**
     * Obtener categoría por ID (público)
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Obtener información de una categoría específica"
    )
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(
            @PathVariable Long id) {

        log.info("Get category by ID request received - id: {}", id);

        CategoryDTO category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.success(category)
        );
    }

    /**
     * Obtener categoría por slug (público)
     * GET /api/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Obtener categoría por slug",
            description = "Obtener información de una categoría por su slug"
    )
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryBySlug(
            @PathVariable String slug) {

        log.info("Get category by slug request received - slug: {}", slug);

        CategoryDTO category = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success(category)
        );
    }

    /**
     * Crear nueva categoría (solo admin)
     * POST /api/categories
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Crear categoría",
            description = "Crear una nueva categoría (solo admin)"
    )
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        log.info("Create category request received - name: {}", request.getName());

        CategoryDTO category = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Categoría creada exitosamente", category));
    }

    /**
     * Actualizar categoría (solo admin)
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualizar una categoría existente (solo admin)"
    )
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        log.info("Update category request received - id: {}", id);

        CategoryDTO category = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Categoría actualizada exitosamente", category)
        );
    }

    /**
     * Eliminar categoría (solo admin)
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Eliminar categoría",
            description = "Eliminar una categoría (solo admin)"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> deleteCategory(
            @PathVariable Long id) {

        log.info("Delete category request received - id: {}", id);

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categoría eliminada exitosamente",
                        MessageResponse.success("Categoría eliminada exitosamente")
                )
        );
    }
}