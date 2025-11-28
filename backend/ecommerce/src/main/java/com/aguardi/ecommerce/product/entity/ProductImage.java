// ============================================
// FILE: src/main/java/com/aguardi/product/entity/ProductImage.java
// Propósito: Entidad de imagen de producto
// ============================================

package com.aguardi.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;

    @Column
    private String publicId;  // ID de Cloudinary para poder eliminar

    @Column
    private String altText;  // Texto alternativo para accesibilidad

    @Column(nullable = false)
    @Builder.Default
    private Boolean isMain = false;  // Imagen principal

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;  // Orden de visualización

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}