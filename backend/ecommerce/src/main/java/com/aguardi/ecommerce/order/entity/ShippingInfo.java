// ============================================
// FILE: src/main/java/com/aguardi/order/entity/ShippingInfo.java
// Propósito: Entidad de información de envío
// ============================================

package com.aguardi.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Datos del destinatario
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    // Dirección de envío
    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String number;

    @Column
    private String floor;

    @Column
    private String apartment;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false, length = 10)
    private String postalCode;

    @Column(length = 500)
    private String reference;

    // Información de tracking
    @Column
    private String trackingNumber;

    @Column
    private String carrier;  // Empresa de transporte (ej: Correo Argentino, OCA)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Método para obtener dirección completa
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(" ").append(number);

        if (floor != null && !floor.isEmpty()) {
            sb.append(", Piso ").append(floor);
        }

        if (apartment != null && !apartment.isEmpty()) {
            sb.append(", Dto. ").append(apartment);
        }

        sb.append(", ").append(city);
        sb.append(", ").append(province);
        sb.append(" (").append(postalCode).append(")");

        return sb.toString();
    }

    // Método para obtener nombre completo
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
