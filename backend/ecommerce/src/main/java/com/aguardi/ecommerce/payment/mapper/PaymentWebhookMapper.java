// ============================================
// FILE: src/main/java/com/aguardi/payment/mapper/PaymentWebhookMapper.java
// Propósito: Mapper para convertir webhooks de MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.mapper;

import com.aguardi.ecommerce.payment.dto.MercadoPagoWebhookDTO;
import com.aguardi.ecommerce.payment.entity.PaymentWebhookLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class PaymentWebhookMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir MercadoPagoWebhookDTO a PaymentWebhookLog
     * @param webhook DTO del webhook
     * @return Entidad de log
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalPaymentId", source = "data.id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "payload", expression = "java(serializeWebhook(webhook))")
    @Mapping(target = "processed", constant = "false")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    public abstract PaymentWebhookLog toEntity(MercadoPagoWebhookDTO webhook);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Serializar webhook a JSON
     * @param webhook DTO del webhook
     * @return JSON string
     */
    protected String serializeWebhook(MercadoPagoWebhookDTO webhook) {
        try {
            return objectMapper.writeValueAsString(webhook);
        } catch (JsonProcessingException e) {
            return "Error al serializar webhook: " + e.getMessage();
        }
    }
}