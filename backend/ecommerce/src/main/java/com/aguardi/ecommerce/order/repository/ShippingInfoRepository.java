// ============================================
// FILE: src/main/java/com/aguardi/order/repository/ShippingInfoRepository.java
// Propósito: Repositorio de información de envío
// ============================================

package com.aguardi.ecommerce.order.repository;

import com.aguardi.ecommerce.order.entity.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Long> {

    /**
     * Buscar información de envío por orden
     * @param orderId ID de la orden
     * @return Optional con la información de envío
     */
    Optional<ShippingInfo> findByOrderId(Long orderId);

    /**
     * Buscar envíos por tracking number
     * @param trackingNumber Número de tracking
     * @return Optional con la información de envío
     */
    Optional<ShippingInfo> findByTrackingNumber(String trackingNumber);

    /**
     * Buscar envíos por provincia
     * @param province Provincia
     * @return Lista de envíos a esa provincia
     */
    List<ShippingInfo> findByProvince(String province);

    /**
     * Buscar envíos por ciudad
     * @param city Ciudad
     * @return Lista de envíos a esa ciudad
     */
    List<ShippingInfo> findByCity(String city);

    /**
     * Buscar envíos por transportista
     * @param carrier Transportista
     * @return Lista de envíos
     */
    List<ShippingInfo> findByCarrier(String carrier);

    /**
     * Contar envíos por provincia (para estadísticas)
     * @return Lista de provincias con cantidad de envíos
     */
    @Query("SELECT si.province, COUNT(si) FROM ShippingInfo si GROUP BY si.province ORDER BY COUNT(si) DESC")
    List<Object[]> countShippingsByProvince();
}