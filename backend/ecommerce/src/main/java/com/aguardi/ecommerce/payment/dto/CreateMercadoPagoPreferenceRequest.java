// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/CreateMercadoPagoPreferenceRequest.java
// Propósito: DTO interno para crear preferencia de pago en MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMercadoPagoPreferenceRequest {

    private List<Item> items;

    private Payer payer;

    @JsonProperty("back_urls")
    private BackUrls backUrls;

    @JsonProperty("auto_return")
    private String autoReturn; // approved, all

    @JsonProperty("external_reference")
    private String externalReference; // Nuestro order ID

    @JsonProperty("notification_url")
    private String notificationUrl; // Webhook URL

    @JsonProperty("statement_descriptor")
    private String statementDescriptor; // Nombre que aparece en resumen de tarjeta

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String title;
        private String description;

        @JsonProperty("picture_url")
        private String pictureUrl;

        @JsonProperty("category_id")
        private String categoryId;

        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unitPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payer {
        private String name;
        private String surname;
        private String email;
        private Phone phone;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Phone {
            @JsonProperty("area_code")
            private String areaCode;

            private String number;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BackUrls {
        private String success;
        private String failure;
        private String pending;
    }
}