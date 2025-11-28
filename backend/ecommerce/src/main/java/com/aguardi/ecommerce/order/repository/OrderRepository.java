// ============================================
// FILE: src/main/java/com/aguardi/order/repository/OrderRepository.java
// Propósito: Repositorio de pedidos con queries complejas
// ============================================

package com.aguardi.ecommerce.order.repository;

import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS
    // ========================================

    /**
     * Buscar orden por número de orden
     * @param orderNumber Número de orden
     * @return Optional con la orden
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Verificar si existe una orden con ese número
     * @param orderNumber Número de orden a verificar
     * @return true si existe, false si no
     */
    boolean existsByOrderNumber(String orderNumber);

    // ========================================
    // BÚSQUEDAS POR USUARIO
    // ========================================

    /**
     * Buscar todas las órdenes de un usuario
     * @param userId ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de órdenes del usuario
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Buscar órdenes de un usuario por estado
     * @param userId ID del usuario
     * @param status Estado de la orden
     * @param pageable Configuración de paginación
     * @return Página de órdenes
     */
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Contar órdenes de un usuario
     * @param userId ID del usuario
     * @return Total de órdenes del usuario
     */
    long countByUserId(Long userId);

    /**
     * Buscar última orden de un usuario
     * @param userId ID del usuario
     * @return Optional con la última orden
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findLastOrderByUserId(@Param("userId") Long userId);

    // ========================================
    // BÚSQUEDAS POR ESTADO
    // ========================================

    /**
     * Buscar órdenes por estado
     * @param status Estado a buscar
     * @param pageable Configuración de paginación
     * @return Página de órdenes
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Buscar órdenes en múltiples estados
     * @param statuses Lista de estados
     * @param pageable Configuración de paginación
     * @return Página de órdenes
     */
    Page<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses, Pageable pageable);

    /**
     * Contar órdenes por estado
     * @param status Estado a contar
     * @return Total de órdenes en ese estado
     */
    long countByStatus(OrderStatus status);

    /**
     * Buscar órdenes pendientes de pago
     * @return Lista de órdenes pendientes
     */
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    // ========================================
    // BÚSQUEDAS POR FECHA
    // ========================================

    /**
     * Buscar órdenes creadas entre dos fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @param pageable Configuración de paginación
     * @return Página de órdenes
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Buscar órdenes pagadas entre dos fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Lista de órdenes pagadas
     */
    List<Order> findByPaidAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar órdenes del día actual
     * @param startOfDay Inicio del día
     * @param endOfDay Fin del día
     * @return Lista de órdenes del día
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay ORDER BY o.createdAt DESC")
    List<Order> findTodayOrders(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // ========================================
    // BÚSQUEDAS POR MONTO
    // ========================================

    /**
     * Buscar órdenes por rango de monto total
     * @param minAmount Monto mínimo
     * @param maxAmount Monto máximo
     * @param pageable Configuración de paginación
     * @return Página de órdenes
     */
    Page<Order> findByTotalBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Buscar órdenes mayores a un monto
     * @param amount Monto mínimo
     * @return Lista de órdenes
     */
    List<Order> findByTotalGreaterThanEqual(BigDecimal amount);

    // ========================================
    // ESTADÍSTICAS Y REPORTES
    // ========================================

    /**
     * Calcular ventas totales por estado
     * @param status Estado de la orden
     * @return Suma total de órdenes en ese estado
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalSalesByStatus(@Param("status") OrderStatus status);

    /**
     * Calcular ventas totales entre fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Suma total de ventas
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSalesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calcular ventas del mes actual
     * @param startOfMonth Inicio del mes
     * @param endOfMonth Fin del mes
     * @return Suma total de ventas del mes
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.paidAt >= :startOfMonth AND o.paidAt < :endOfMonth")
    BigDecimal calculateMonthlyRevenue(@Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * Contar órdenes creadas hoy
     * @param startOfDay Inicio del día
     * @return Total de órdenes del día
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
    long countTodayOrders(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Obtener ticket promedio (monto promedio de órdenes)
     * @return Monto promedio
     */
    @Query("SELECT AVG(o.total) FROM Order o WHERE o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
    BigDecimal calculateAverageOrderValue();

    /**
     * Buscar órdenes con productos específicos
     * @param productId ID del producto
     * @return Lista de órdenes que contienen ese producto
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.id = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") Long productId);

    /**
     * Top clientes por monto gastado
     * @param limit Cantidad de resultados
     * @return Lista de user IDs con su total gastado
     */
    @Query("SELECT o.user.id, SUM(o.total) as totalSpent FROM Order o " +
            "WHERE o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED') " +
            "GROUP BY o.user.id " +
            "ORDER BY totalSpent DESC " +
            "LIMIT :limit")
    List<Object[]> findTopCustomers(@Param("limit") int limit);

    // ========================================
    // ACTUALIZACIONES
    // ========================================

    /**
     * Actualizar estado de una orden
     * @param orderId ID de la orden
     * @param newStatus Nuevo estado
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus, o.updatedAt = CURRENT_TIMESTAMP WHERE o.id = :orderId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("newStatus") OrderStatus newStatus);

    /**
     * Marcar orden como pagada
     * @param orderId ID de la orden
     * @param paidAt Fecha y hora del pago
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'PAID', o.paidAt = :paidAt WHERE o.id = :orderId")
    void markAsPaid(@Param("orderId") Long orderId, @Param("paidAt") LocalDateTime paidAt);

    /**
     * Marcar orden como enviada
     * @param orderId ID de la orden
     * @param shippedAt Fecha y hora del envío
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'SHIPPED', o.shippedAt = :shippedAt WHERE o.id = :orderId")
    void markAsShipped(@Param("orderId") Long orderId, @Param("shippedAt") LocalDateTime shippedAt);

    /**
     * Marcar orden como entregada
     * @param orderId ID de la orden
     * @param deliveredAt Fecha y hora de entrega
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'DELIVERED', o.deliveredAt = :deliveredAt WHERE o.id = :orderId")
    void markAsDelivered(@Param("orderId") Long orderId, @Param("deliveredAt") LocalDateTime deliveredAt);

    /**
     * Cancelar orden
     * @param orderId ID de la orden
     * @param cancelledAt Fecha y hora de cancelación
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'CANCELLED', o.cancelledAt = :cancelledAt WHERE o.id = :orderId")
    void cancelOrder(@Param("orderId") Long orderId, @Param("cancelledAt") LocalDateTime cancelledAt);
}

