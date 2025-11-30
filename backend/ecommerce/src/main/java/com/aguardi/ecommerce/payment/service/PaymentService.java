// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/payment/service/PaymentService.java
// Propósito: Interface del servicio de pagos con MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.service;

import com.aguardi.ecommerce.payment.dto.*;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface PaymentService {

    // ========================================
    // GESTIÓN DE PAGOS
    // ========================================

    /**
     * Crear un pago y generar preferencia de MercadoPago
     * @param request Datos del pago
     * @return Respuesta con URL de pago
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Obtener pago por ID
     * @param paymentId ID del pago
     * @return DTO del pago
     */
    PaymentDTO getPaymentById(Long paymentId);

    /**
     * Obtener pago por ID de orden
     * @param orderId ID de la orden
     * @return DTO del pago
     */
    PaymentDTO getPaymentByOrderId(Long orderId);

    /**
     * Obtener pago por ID externo de MercadoPago
     * @param externalPaymentId ID del pago en MercadoPago
     * @return DTO del pago
     */
    PaymentDTO getPaymentByExternalId(String externalPaymentId);

    /**
     * Obtener todos los pagos del usuario autenticado
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<PaymentDTO> getCurrentUserPayments(Pageable pageable);

    /**
     * Obtener todos los pagos (Admin)
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<PaymentDTO> getAllPayments(Pageable pageable);

    /**
     * Obtener pagos por estado (Admin)
     * @param status Estado del pago
     * @param pageable Configuración de paginación
     * @return Página de pagos
     */
    Page<PaymentDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    // ========================================
    // MERCADOPAGO
    // ========================================

    /**
     * Crear preferencia de pago en MercadoPago
     * @param orderId ID de la orden
     * @return Respuesta con init_point y preference_id
     */
    PaymentResponse createMercadoPagoPreference(Long orderId);

    /**
     * Procesar webhook de MercadoPago
     * @param webhookData Datos del webhook
     */
    void processMercadoPagoWebhook(Map<String, Object> webhookData);

    /**
     * Verificar estado de pago en MercadoPago
     * @param paymentId ID externo del pago
     * @return DTO con estado actualizado
     */
    PaymentStatusDTO checkPaymentStatus(String paymentId);

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    /**
     * Reembolsar un pago
     * @param paymentId ID del pago
     * @param request Datos del reembolso
     * @return DTO del pago reembolsado
     */
    PaymentDTO refundPayment(Long paymentId, RefundPaymentRequest request);

    /**
     * Cancelar un pago pendiente
     * @param paymentId ID del pago
     * @return DTO del pago cancelado
     */
    PaymentDTO cancelPayment(Long paymentId);

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * Calcular ingresos totales
     * @return Monto total de ingresos
     */
    BigDecimal calculateTotalRevenue();

    /**
     * Calcular ingresos del mes actual
     * @return Monto total del mes
     */
    BigDecimal calculateMonthlyRevenue();

    /**
     * Calcular ingresos entre fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Monto total del período
     */
    BigDecimal calculateRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Obtener tasa de aprobación
     * @return Porcentaje de pagos aprobados
     */
    Double getApprovalRate();

    /**
     * Obtener pagos pendientes antiguos (para verificar)
     * @param minutesOld Antigüedad en minutos
     * @return Lista de pagos pendientes
     */
    Page<PaymentDTO> getStalePendingPayments(int minutesOld, Pageable pageable);
}