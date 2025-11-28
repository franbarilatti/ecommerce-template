// ============================================
// FILE: src/main/java/com/aguardi/payment/repository/PaymentRepository.java
// Propósito: Repositorio de pagos con queries para integración con MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.repository;

import com.aguardi.ecommerce.payment.entity.Payment;
import com.aguardi.ecommerce.payment.entity.PaymentMethod;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS
    // ========================================

    /**
     * Buscar pago por ID de orden
     * @param orderId ID de la orden
     * @return Optional con el pago
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Buscar pago por ID externo de MercadoPago
     * @param externalPaymentId ID del pago en MercadoPago
     * @return Optional con el pago
     */
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);

    /**
     * Buscar pago por preference ID de MercadoPago
     * @param preferenceId ID de la preferencia
     * @return Optional con el pago
     */
    Optional<Payment> findByPreferenceId(String preferenceId);

    /**
     * Verificar si existe un pago con ese ID externo
     * @param externalPaymentId ID externo a verificar
     * @return true si existe, false si no
     */
    boolean existsByExternalPaymentId(String externalPaymentId);

    // ========================================
    // BÚSQUEDAS POR USUARIO
    // ========================================

    /**
     * Buscar todos los pagos de un usuario
     * @param userId ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de pagos del usuario
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Buscar pagos de un usuario por estado
     * @param userId ID del usuario
     * @param status Estado del pago
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, PaymentStatus status, Pageable pageable);

    /**
     * Contar pagos de un usuario
     * @param userId ID del usuario
     * @return Total de pagos del usuario
     */
    long countByUserId(Long userId);

    /**
     * Calcular monto total pagado por un usuario
     * @param userId ID del usuario
     * @return Suma total de pagos aprobados
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.status = 'APPROVED'")
    BigDecimal calculateTotalPaidByUser(@Param("userId") Long userId);

    // ========================================
    // BÚSQUEDAS POR ESTADO
    // ========================================

    /**
     * Buscar pagos por estado
     * @param status Estado a buscar
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Buscar pagos en múltiples estados
     * @param statuses Lista de estados
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByStatusInOrderByCreatedAtDesc(List<PaymentStatus> statuses, Pageable pageable);

    /**
     * Contar pagos por estado
     * @param status Estado a contar
     * @return Total de pagos en ese estado
     */
    long countByStatus(PaymentStatus status);

    /**
     * Buscar pagos pendientes (para verificar)
     * @return Lista de pagos pendientes
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'IN_PROCESS') ORDER BY p.createdAt ASC")
    List<Payment> findPendingPayments();

    /**
     * Buscar pagos pendientes antiguos (más de X minutos)
     * @param time Tiempo límite
     * @return Lista de pagos pendientes antiguos
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :time")
    List<Payment> findStalePendingPayments(@Param("time") LocalDateTime time);

    // ========================================
    // BÚSQUEDAS POR MÉTODO DE PAGO
    // ========================================

    /**
     * Buscar pagos por método de pago
     * @param method Método de pago
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByMethodOrderByCreatedAtDesc(PaymentMethod method, Pageable pageable);

    /**
     * Contar pagos por método
     * @param method Método de pago
     * @return Total de pagos con ese método
     */
    long countByMethod(PaymentMethod method);

    /**
     * Estadísticas de métodos de pago
     * @return Lista de métodos con cantidad de pagos
     */
    @Query("SELECT p.method, COUNT(p) FROM Payment p WHERE p.status = 'APPROVED' GROUP BY p.method ORDER BY COUNT(p) DESC")
    List<Object[]> countPaymentsByMethod();

    // ========================================
    // BÚSQUEDAS POR FECHA
    // ========================================

    /**
     * Buscar pagos creados entre dos fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Buscar pagos aprobados entre dos fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Lista de pagos aprobados
     */
    List<Payment> findByApprovedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar pagos del día actual
     * @param startOfDay Inicio del día
     * @param endOfDay Fin del día
     * @return Lista de pagos del día
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay ORDER BY p.createdAt DESC")
    List<Payment> findTodayPayments(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // ========================================
    // BÚSQUEDAS POR MONTO
    // ========================================

    /**
     * Buscar pagos por rango de monto
     * @param minAmount Monto mínimo
     * @param maxAmount Monto máximo
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Buscar pagos mayores a un monto
     * @param amount Monto mínimo
     * @return Lista de pagos
     */
    List<Payment> findByAmountGreaterThanEqual(BigDecimal amount);

    // ========================================
    // ESTADÍSTICAS Y REPORTES
    // ========================================

    /**
     * Calcular ingresos totales por estado
     * @param status Estado del pago
     * @return Suma total de pagos en ese estado
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal calculateTotalRevenueByStatus(@Param("status") PaymentStatus status);

    /**
     * Calcular ingresos totales entre fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Suma total de ingresos
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.approvedAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calcular ingresos del mes actual
     * @param startOfMonth Inicio del mes
     * @param endOfMonth Fin del mes
     * @return Suma total de ingresos del mes
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.approvedAt >= :startOfMonth AND p.approvedAt < :endOfMonth")
    BigDecimal calculateMonthlyRevenue(@Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * Calcular ticket promedio de pagos aprobados
     * @return Monto promedio
     */
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = 'APPROVED'")
    BigDecimal calculateAveragePaymentAmount();

    /**
     * Contar pagos del día
     * @param startOfDay Inicio del día
     * @return Total de pagos del día
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :startOfDay")
    long countTodayPayments(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Contar pagos aprobados del día
     * @param startOfDay Inicio del día
     * @return Total de pagos aprobados del día
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'APPROVED' AND p.approvedAt >= :startOfDay")
    long countTodayApprovedPayments(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Tasa de aprobación de pagos (porcentaje)
     * @return Tasa de aprobación entre 0 y 1
     */
    @Query("SELECT CAST(COUNT(CASE WHEN p.status = 'APPROVED' THEN 1 END) AS double) / COUNT(p) FROM Payment p")
    Double calculateApprovalRate();

    // ========================================
    // ACTUALIZACIONES
    // ========================================

    /**
     * Actualizar estado de un pago
     * @param paymentId ID del pago
     * @param newStatus Nuevo estado
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :paymentId")
    void updatePaymentStatus(@Param("paymentId") Long paymentId, @Param("newStatus") PaymentStatus newStatus);

    /**
     * Marcar pago como aprobado
     * @param paymentId ID del pago
     * @param approvedAt Fecha y hora de aprobación
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = 'APPROVED', p.approvedAt = :approvedAt WHERE p.id = :paymentId")
    void markAsApproved(@Param("paymentId") Long paymentId, @Param("approvedAt") LocalDateTime approvedAt);

    /**
     * Marcar pago como rechazado
     * @param paymentId ID del pago
     * @param rejectedAt Fecha y hora de rechazo
     * @param statusDetail Detalle del rechazo
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = 'REJECTED', p.rejectedAt = :rejectedAt, p.statusDetail = :statusDetail WHERE p.id = :paymentId")
    void markAsRejected(@Param("paymentId") Long paymentId, @Param("rejectedAt") LocalDateTime rejectedAt, @Param("statusDetail") String statusDetail);

    /**
     * Actualizar información desde webhook de MercadoPago
     * @param paymentId ID del pago
     * @param externalPaymentId ID externo de MercadoPago
     * @param status Estado del pago
     * @param statusDetail Detalle del estado
     */
    @Modifying
    @Query("UPDATE Payment p SET p.externalPaymentId = :externalPaymentId, p.status = :status, p.statusDetail = :statusDetail WHERE p.id = :paymentId")
    void updateFromWebhook(
            @Param("paymentId") Long paymentId,
            @Param("externalPaymentId") String externalPaymentId,
            @Param("status") PaymentStatus status,
            @Param("statusDetail") String statusDetail
    );
}
