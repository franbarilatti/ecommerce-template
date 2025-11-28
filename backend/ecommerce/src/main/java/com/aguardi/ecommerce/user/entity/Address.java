// ============================================
// FILE: src/main/java/com/aguardi/user/entity/Address.java
// Propósito: Entidad de dirección de envío
// ============================================

package com.aguardi.ecommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column
    private String reference;  // Referencias adicionales (ej: "portón verde")

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;  // Dirección por defecto

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Método para obtener dirección completa formateada
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
}
