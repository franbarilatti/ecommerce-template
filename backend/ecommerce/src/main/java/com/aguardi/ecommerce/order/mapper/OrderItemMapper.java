// ============================================
// FILE: src/main/java/com/aguardi/order/mapper/OrderItemMapper.java
// Propósito: Mapper para convertir entre OrderItem Entity y DTOs
// ============================================

package com.aguardi.ecommerce.order.mapper;

import com.aguardi.ecommerce.order.dto.CreateOrderItemRequest;
import com.aguardi.ecommerce.order.dto.OrderItemDTO;
import com.aguardi.ecommerce.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderItemMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir OrderItem Entity a OrderItemDTO
     * @param item Entidad de item de orden
     * @return DTO de item
     */
    @Mapping(target = "productId", source = "product.id")
    OrderItemDTO toDTO(OrderItem item);

    /**
     * Convertir lista de OrderItem a lista de OrderItemDTO
     * @param items Lista de entidades
     * @return Lista de DTOs
     */
    List<OrderItemDTO> toDTOList(List<OrderItem> items);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateOrderItemRequest a OrderItem Entity
     * @param request Request con datos de nuevo item
     * @return Nueva entidad de item
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Se setea en el servicio
    @Mapping(target = "product", ignore = true) // Se busca en el servicio
    @Mapping(target = "productName", ignore = true) // Se obtiene del producto
    @Mapping(target = "productPrice", ignore = true) // Se obtiene del producto
    @Mapping(target = "lineTotal", ignore = true) // Se calcula automático
    @Mapping(target = "productImageUrl", ignore = true) // Se obtiene del producto
    OrderItem toEntity(CreateOrderItemRequest request);
}