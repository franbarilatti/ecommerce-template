// ============================================
// FILE: src/main/java/com/aguardi/order/entity/Order.java
// Propósito: Entidad principal de Pedido
// ============================================

package com.aguardi.ecommerce.order.entity;

import com.aguardi.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_number", columnList = "order_number"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;  // Número de orden visible (ej: ORD-20240101-ABC123)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Items del pedido
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // Información de envío
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShippingInfo shippingInfo;

    // Montos
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;  // Suma de items

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;  // subtotal + shipping - discount

    // Notas
    @Column(length = 500)
    private String customerNotes;  // Notas del cliente

    @Column(length = 500)
    private String adminNotes;  // Notas internas del admin

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime cancelledAt;

    // Métodos helper para manejar items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // Método para calcular subtotal
    public void calculateSubtotal() {
        this.subtotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para calcular total
    public void calculateTotal() {
        this.total = subtotal
                .add(shippingCost)
                .subtract(discount);
    }

    // Método para generar número de orden
    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String timestamp = String.valueOf(System.currentTimeMillis());
            this.orderNumber = "ORD-" + timestamp.substring(timestamp.length() - 6) + "-" + uuid;
        }
    }

    // Método para verificar si la orden puede ser cancelada
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.PROCESSING;
    }

    // Método para verificar si la orden está pagada
    public boolean isPaid() {
        return status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING ||
                status == OrderStatus.SHIPPED ||
                status == OrderStatus.DELIVERED;
    }
}