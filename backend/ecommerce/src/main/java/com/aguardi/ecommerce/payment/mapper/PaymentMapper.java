// ============================================
// FILE: src/main/java/com/aguardi/payment/mapper/PaymentMapper.java
// Propósito: Mapper para convertir entre Payment Entity y DTOs
// ============================================

package com.aguardi.ecommerce.payment.mapper;

import com.aguardi.ecommerce.order.dto.PaymentInfoDTO;
import com.aguardi.ecommerce.payment.dto.*;
import com.aguardi.ecommerce.payment.entity.Payment;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir Payment Entity a PaymentDTO
     * @param payment Entidad de pago
     * @return DTO de pago
     */
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "userId", source = "user.id")
    PaymentDTO toDTO(Payment payment);

    /**
     * Convertir lista de Payment a lista de PaymentDTO
     * @param payments Lista de entidades
     * @return Lista de DTOs
     */
    List<PaymentDTO> toDTOList(List<Payment> payments);

    /**
     * Convertir Payment Entity a PaymentStatusDTO
     * @param payment Entidad de pago
     * @return DTO de estado de pago
     */
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "successful", expression = "java(payment.isSuccessful())")
    @Mapping(target = "pending", expression = "java(payment.isPending())")
    @Mapping(target = "canBeRetried", expression = "java(payment.isRejected())")
    PaymentStatusDTO toStatusDTO(Payment payment);

    /**
     * Convertir Payment Entity a PaymentInfoDTO (para incluir en OrderDetailDTO)
     * @param payment Entidad de pago
     * @return DTO de información de pago
     */
    @Mapping(target = "paymentId", source = "id")
    PaymentInfoDTO toInfoDTO(Payment payment);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir PaymentRequest a Payment Entity
     * @param request Request con datos de nuevo pago
     * @return Nueva entidad de pago
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Se setea en el servicio
    @Mapping(target = "user", ignore = true) // Se setea en el servicio
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "externalPaymentId", ignore = true)
    @Mapping(target = "preferenceId", ignore = true)
    @Mapping(target = "merchantOrderId", ignore = true)
    @Mapping(target = "paymentDetails", ignore = true)
    @Mapping(target = "statusDetail", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "refundedAt", ignore = true)
    Payment toEntity(PaymentRequest request);

    /**
     * Actualizar Payment desde información de MercadoPago
     * @param mpPayment DTO con información de MercadoPago
     * @param payment Entidad existente a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "method", ignore = true)
    @Mapping(target = "externalPaymentId", source = "id")
    @Mapping(target = "status", expression = "java(mapMercadoPagoStatus(mpPayment.getStatus()))")
    @Mapping(target = "statusDetail", source = "statusDetail")
    @Mapping(target = "merchantOrderId", source = "merchantOrderId")
    @Mapping(target = "preferenceId", ignore = true)
    @Mapping(target = "paymentDetails", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "refundedAt", ignore = true)
    void updateFromMercadoPago(MercadoPagoPaymentDTO mpPayment, @MappingTarget Payment payment);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Mapear estado de MercadoPago a PaymentStatus interno
     * @param mpStatus Estado de MercadoPago
     * @return Estado interno
     */
    default PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        if (mpStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "charged_back" -> PaymentStatus.CHARGED_BACK;
            case "in_process" -> PaymentStatus.IN_PROCESS;
            default -> PaymentStatus.PENDING;
        };
    }

    /**
     * Enriquecer PaymentResponse con información adicional
     * @param response Response a enriquecer
     * @param payment Entidad de pago
     */
    @AfterMapping
    default void enrichPaymentResponse(@MappingTarget PaymentResponse response, Payment payment) {
        if (payment.getOrder() != null) {
            response.setOrderNumber(payment.getOrder().getOrderNumber());
        }
    }
}