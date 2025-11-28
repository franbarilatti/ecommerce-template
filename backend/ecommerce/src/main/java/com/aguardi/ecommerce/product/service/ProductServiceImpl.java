// ============================================
// FILE: src/main/java/com/aguardi/product/service/ProductServiceImpl.java
// Propósito: Implementación del servicio de productos
// ============================================

package com.aguardi.ecommerce.product.service;

import com.aguardi.ecommerce.product.dto.*;
import com.aguardi.ecommerce.product.entity.Category;
import com.aguardi.ecommerce.product.entity.Product;
import com.aguardi.ecommerce.product.mapper.ProductMapper;
import com.aguardi.ecommerce.product.repository.CategoryRepository;
import com.aguardi.ecommerce.product.repository.ProductRepository;
import com.aguardi.ecommerce.shared.exception.BadRequestException;
import com.aguardi.ecommerce.shared.exception.ConflictException;
import com.aguardi.ecommerce.shared.exception.InsufficientStockException;
import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Getting all products with pagination");

        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(ProductFilterRequest filter) {
        log.info("Searching products with filters: {}", filter);

        // Crear ordenamiento
        Sort sort = filter.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Buscar con filtros
        Page<Product> products;

        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            // Búsqueda por texto
            products = productRepository.searchProducts(filter.getSearchTerm(), pageable);
        } else if (filter.getCategorySlug() != null) {
            // Buscar por slug de categoría
            products = productRepository.findByCategorySlug(filter.getCategorySlug(), pageable);
        } else {
            // Aplicar filtros múltiples
            products = productRepository.findWithFilters(
                    filter.getCategoryId(),
                    filter.getMinPrice(),
                    filter.getMaxPrice(),
                    filter.getIsNew(),
                    filter.getOnSale(),
                    pageable
            );
        }

        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting products by category: {}", categoryId);

        // Verificar que la categoría existe
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoría", "id", categoryId);
        }

        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategorySlug(String categorySlug, Pageable pageable) {
        log.info("Getting products by category slug: {}", categorySlug);

        // Verificar que la categoría existe
        if (!categoryRepository.existsBySlug(categorySlug)) {
            throw new ResourceNotFoundException("Categoría", "slug", categorySlug);
        }

        Page<Product> products = productRepository.findByCategorySlug(categorySlug, pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getNewProducts(Pageable pageable) {
        log.info("Getting new products");

        Page<Product> products = productRepository.findByIsNewTrueAndActiveTrue(pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsOnSale(Pageable pageable) {
        log.info("Getting products on sale");

        Page<Product> products = productRepository.findByOnSaleTrueAndActiveTrue(pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {
        log.info("Getting product detail by ID: {}", productId);

        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        ProductDetailDTO detail = productMapper.toDetailDTO(product);

        // Agregar productos relacionados
        List<Product> relatedProducts = productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                PageRequest.of(0, 4)
        );
        detail.setRelatedProducts(productMapper.toDTOList(relatedProducts));

        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getRelatedProducts(Long productId, int limit) {
        log.info("Getting related products for: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        List<Product> relatedProducts = productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                PageRequest.of(0, limit)
        );

        return productMapper.toDTOList(relatedProducts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getBestSellers(int limit) {
        log.info("Getting best sellers");

        List<Product> products = productRepository.findBestSellers(
                PageRequest.of(0, limit)
        );

        return productMapper.toDTOList(products);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        // Verificar que la categoría existe
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoryId()));

        // Verificar SKU único si se proporciona
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("Ya existe un producto con ese SKU");
        }

        // Crear producto
        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        product = productRepository.save(product);

        log.info("Product created successfully: {}", product.getId());

        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        // Verificar categoría si cambió
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        // Verificar SKU único si cambió
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new ConflictException("Ya existe un producto con ese SKU");
            }
        }

        // Actualizar datos
        productMapper.updateEntityFromDTO(request, product);
        product = productRepository.save(product);

        log.info("Product updated successfully: {}", productId);

        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateStock(Long productId, Integer newStock) {
        log.info("Updating stock for product: {} to {}", productId, newStock);

        if (newStock < 0) {
            throw new BadRequestException("El stock no puede ser negativo");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        product.setStock(newStock);
        product = productRepository.save(product);

        log.info("Stock updated successfully for product: {}", productId);

        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        // Soft delete (desactivar en lugar de eliminar)
        product.setActive(false);
        productRepository.save(product);

        log.info("Product deleted (deactivated) successfully: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, Integer quantity) {
        log.debug("Checking stock for product: {} quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productId));

        return product.hasStock(quantity);
    }
}