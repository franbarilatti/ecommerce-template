// ============================================
// FILE: src/main/java/com/aguardi/product/service/CategoryServiceImpl.java
// Propósito: Implementación del servicio de categorías
// ============================================

package com.aguardi.ecommerce.product.service;

import com.aguardi.ecommerce.product.dto.CategoryDTO;
import com.aguardi.ecommerce.product.dto.CreateCategoryRequest;
import com.aguardi.ecommerce.product.dto.UpdateCategoryRequest;
import com.aguardi.ecommerce.product.entity.Category;
import com.aguardi.ecommerce.product.mapper.CategoryMapper;
import com.aguardi.ecommerce.product.repository.CategoryRepository;
import com.aguardi.ecommerce.shared.exception.BadRequestException;
import com.aguardi.ecommerce.shared.exception.ConflictException;
import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        log.info("Getting all active categories");

        List<Category> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        return categoryMapper.toDTOList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Getting all categories");

        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        return categoryMapper.toDTOList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesWithProducts() {
        log.info("Getting categories with products");

        List<Category> categories = categoryRepository.findCategoriesWithProducts();
        return categoryMapper.toDTOList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long categoryId) {
        log.info("Getting category by ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoryId));

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        log.info("Getting category by slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "slug", slug));

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Verificar que el nombre no exista
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        // Verificar que el slug no exista
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Ya existe una categoría con ese slug");
        }

        // Crear categoría
        Category category = categoryMapper.toEntity(request);
        category = categoryRepository.save(category);

        log.info("Category created successfully: {}", category.getId());

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long categoryId, UpdateCategoryRequest request) {
        log.info("Updating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoryId));

        // Verificar que el nombre no exista (si cambió)
        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.findByName(request.getName()).isPresent()) {
                throw new ConflictException("Ya existe una categoría con ese nombre");
            }
        }

        // Actualizar datos
        categoryMapper.updateEntityFromDTO(request, category);
        category = categoryRepository.save(category);

        log.info("Category updated successfully: {}", categoryId);

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoryId));

        // Verificar si tiene productos
        if (category.getActiveProductCount() > 0) {
            throw new BadRequestException(
                    "No se puede eliminar una categoría que tiene productos asociados"
            );
        }

        categoryRepository.delete(category);

        log.info("Category deleted successfully: {}", categoryId);
    }
}