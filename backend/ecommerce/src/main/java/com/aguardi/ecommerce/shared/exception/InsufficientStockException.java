// ============================================
// FILE: src/main/java/com/aguardi/shared/exception/InsufficientStockException.java
// Propósito: Excepción para stock insuficiente
// ============================================

package com.aguardi.ecommerce.shared.exception;

public class InsufficientStockException extends RuntimeException {

    private Long productId;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableStock;

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long productId, String productName,
                                      Integer requestedQuantity, Integer availableStock) {
        super(String.format(
                "Stock insuficiente para '%s'. Solicitado: %d, Disponible: %d",
                productName, requestedQuantity, availableStock
        ));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}