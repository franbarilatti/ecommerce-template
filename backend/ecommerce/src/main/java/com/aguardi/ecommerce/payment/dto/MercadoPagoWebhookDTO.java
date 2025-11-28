// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/MercadoPagoWebhookDTO.java
// Propósito: DTO para recibir webhooks de MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MercadoPagoWebhookDTO {

    private String id;

    @JsonProperty("live_mode")
    private Boolean liveMode;

    private String type; // payment, merchant_order

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("user_id")
    private String userId;

    private Long version;

    private String action; // payment.created, payment.updated

    @JsonProperty("api_version")
    private String apiVersion;

    private WebhookData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookData {
        private String id; // ID del pago o merchant order
    }
}