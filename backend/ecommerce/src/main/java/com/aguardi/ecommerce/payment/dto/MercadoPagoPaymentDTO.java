// ============================================
// FILE: src/main/java/com/aguardi/payment/dto/MercadoPagoPaymentDTO.java
// Propósito: DTO para información detallada de pago de MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MercadoPagoPaymentDTO {

    private Long id;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("date_approved")
    private String dateApproved;

    @JsonProperty("date_last_updated")
    private String dateLastUpdated;

    private String status; // approved, rejected, pending, etc.

    @JsonProperty("status_detail")
    private String statusDetail;

    @JsonProperty("operation_type")
    private String operationType;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;

    @JsonProperty("payment_type_id")
    private String paymentTypeId;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("transaction_amount_refunded")
    private BigDecimal transactionAmountRefunded;

    private String description;

    @JsonProperty("external_reference")
    private String externalReference; // Nuestro order ID

    @JsonProperty("merchant_order_id")
    private Long merchantOrderId;

    private Payer payer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {
        private String id;
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
    }
}