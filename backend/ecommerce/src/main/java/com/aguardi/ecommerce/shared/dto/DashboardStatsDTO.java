// ============================================
// FILE: src/main/java/com/aguardi/shared/dto/DashboardStatsDTO.java
// Propósito: DTO para estadísticas del dashboard admin
// ============================================

package com.aguardi.ecommerce.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    // Estadísticas generales
    private Long totalUsers;
    private Long totalProducts;
    private Long totalOrders;
    private Long totalSales;

    // Estadísticas de hoy
    private Long todayOrders;
    private Long todayUsers;
    private BigDecimal todayRevenue;

    // Estadísticas del mes
    private Long monthOrders;
    private BigDecimal monthRevenue;
    private Long monthNewUsers;

    // Productos
    private Long productsInStock;
    private Long productsOutOfStock;
    private Long productsLowStock;

    // Órdenes por estado
    private Map<String, Long> ordersByStatus;

    // Productos más vendidos
    private List<TopProductDTO> topProducts;

    // Ventas por día (últimos 7 días)
    private List<DailySalesDTO> dailySales;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private Long totalSold;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySalesDTO {
        private String date;
        private Long orders;
        private BigDecimal revenue;
    }
}
