// ============================================
// FILE: src/main/java/com/aguardi/product/entity/Product.java
// Propósito: Entidad principal de Producto
// ============================================

package com.aguardi.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;  // Precio en oferta

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;  // Peso en kg para calcular envío

    @Column(nullable = false)
    @Builder.Default
    private Boolean isNew = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean onSale = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 100)
    private String sku;  // Código de producto

    // Relación con imágenes
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Campos calculados (no persistidos)
    @Transient
    private Integer soldCount;  // Para estadísticas

    @Transient
    private Double averageRating;  // Para reviews (futuro)

    // Métodos helper para manejar imágenes
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    // Método para obtener imagen principal
    public ProductImage getMainImage() {
        return images.stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    // Método para verificar si hay stock disponible
    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    // Método para reducir stock
    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Stock insuficiente");
        }
        this.stock -= quantity;
    }

    // Método para restaurar stock (en caso de cancelación)
    public void restoreStock(int quantity) {
        this.stock += quantity;
    }

    // Método para obtener precio efectivo (con descuento si está en oferta)
    public BigDecimal getEffectivePrice() {
        return (onSale && salePrice != null) ? salePrice : price;
    }

    // Método para calcular porcentaje de descuento
    public Integer getDiscountPercentage() {
        if (!onSale || salePrice == null || salePrice.compareTo(price) >= 0) {
            return 0;
        }
        BigDecimal discount = price.subtract(salePrice);
        BigDecimal percentage = discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, BigDecimal.ROUND_HALF_UP);
        return percentage.intValue();
    }
}
