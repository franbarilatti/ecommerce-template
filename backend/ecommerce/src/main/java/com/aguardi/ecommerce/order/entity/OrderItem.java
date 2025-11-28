// ============================================
// FILE: src/main/java/com/aguardi/order/entity/OrderItem.java
// Propósito: Entidad de item de pedido
// ============================================

package com.aguardi.ecommerce.order.entity;

import com.aguardi.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName;  // Guardamos el nombre por si se elimina el producto

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;  // Precio al momento de la compra

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;  // quantity * productPrice

    @Column
    private String productImageUrl;  // Guardamos la imagen principal

    // Calcular total de la línea antes de persistir
    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {
        this.lineTotal = productPrice.multiply(BigDecimal.valueOf(quantity));
    }
}