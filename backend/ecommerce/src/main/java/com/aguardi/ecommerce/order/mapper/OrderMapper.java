// ============================================
// FILE: src/main/java/com/aguardi/order/mapper/OrderMapper.java
// Propósito: Mapper para convertir entre Order Entity y DTOs
// ============================================

package com.aguardi.ecommerce.order.mapper;

import com.aguardi.ecommerce.order.dto.*;
import com.aguardi.ecommerce.order.entity.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {OrderItemMapper.class, ShippingInfoMapper.class}
)
public interface OrderMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir Order Entity a OrderDTO (versión básica para listados)
     * @param order Entidad de orden
     * @return DTO básico de orden
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "itemCount", expression = "java(order.getItems().size())")
    OrderDTO toDTO(Order order);

    /**
     * Convertir lista de Order a lista de OrderDTO
     * @param orders Lista de entidades
     * @return Lista de DTOs
     */
    List<OrderDTO> toDTOList(List<Order> orders);

    /**
     * Convertir Order Entity a OrderDetailDTO (versión completa)
     * @param order Entidad de orden
     * @return DTO completo de orden
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userFullName", expression = "java(order.getUser().getFullName())")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "shippingInfo", source = "shippingInfo")
    @Mapping(target = "paymentInfo", ignore = true) // Se setea en el servicio si es necesario
    OrderDetailDTO toDetailDTO(Order order);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateOrderRequest a Order Entity
     * @param request Request con datos de nueva orden
     * @return Nueva entidad de orden
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true) // Se genera automático
    @Mapping(target = "user", ignore = true) // Se setea en el servicio
    @Mapping(target = "status", ignore = true) // Se setea en el servicio
    @Mapping(target = "items", ignore = true) // Se crean en el servicio
    @Mapping(target = "shippingInfo", ignore = true) // Se crea en el servicio
    @Mapping(target = "subtotal", ignore = true) // Se calcula
    @Mapping(target = "shippingCost", ignore = true) // Se calcula
    @Mapping(target = "discount", ignore = true) // Se calcula
    @Mapping(target = "total", ignore = true) // Se calcula
    @Mapping(target = "adminNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "shippedAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    Order toEntity(CreateOrderRequest request);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Enriquecer DTO con información adicional
     */
    @AfterMapping
    default void enrichOrderDTO(@MappingTarget OrderDTO dto, Order order) {
        // Los mappings ya están en las anotaciones
    }
}