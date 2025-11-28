// ============================================
// FILE: src/main/java/com/aguardi/order/mapper/ShippingInfoMapper.java
// Propósito: Mapper para convertir entre ShippingInfo Entity y DTOs
// ============================================

package com.aguardi.ecommerce.order.mapper;

import com.aguardi.order.dto.CreateShippingInfoRequest;
import com.aguardi.ecommerce.order.dto.ShippingInfoDTO;
import com.aguardi.ecommerce.order.entity.ShippingInfo;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ShippingInfoMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir ShippingInfo Entity a ShippingInfoDTO
     * @param shippingInfo Entidad de información de envío
     * @return DTO de información de envío
     */
    ShippingInfoDTO toDTO(ShippingInfo shippingInfo);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateShippingInfoRequest a ShippingInfo Entity
     * @param request Request con datos de envío
     * @return Nueva entidad de información de envío
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Se setea en el servicio
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShippingInfo toEntity(CreateShippingInfoRequest request);

    /**
     * Actualizar ShippingInfo Entity desde UpdateOrderStatusRequest
     * @param trackingNumber Número de tracking
     * @param carrier Transportista
     * @param shippingInfo Entidad existente a actualizar
     */
    default void updateTrackingInfo(String trackingNumber, String carrier, @MappingTarget ShippingInfo shippingInfo) {
        if (trackingNumber != null) {
            shippingInfo.setTrackingNumber(trackingNumber);
        }
        if (carrier != null) {
            shippingInfo.setCarrier(carrier);
        }
    }
}