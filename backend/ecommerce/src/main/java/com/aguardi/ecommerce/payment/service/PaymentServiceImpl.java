// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/payment/service/PaymentServiceImpl.java
// Propósito: Implementación del servicio de pagos con MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.service;

import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.order.entity.OrderStatus;
import com.aguardi.ecommerce.order.repository.OrderRepository;
import com.aguardi.ecommerce.payment.dto.*;
import com.aguardi.ecommerce.payment.entity.Payment;
import com.aguardi.ecommerce.payment.entity.PaymentMethod;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import com.aguardi.ecommerce.payment.entity.PaymentWebhookLog;
import com.aguardi.ecommerce.payment.mapper.PaymentMapper;
import com.aguardi.ecommerce.payment.mapper.PaymentWebhookMapper;
import com.aguardi.ecommerce.payment.repository.PaymentRepository;
import com.aguardi.ecommerce.payment.repository.PaymentWebhookLogRepository;
import com.aguardi.ecommerce.shared.exception.*;
import com.aguardi.ecommerce.shared.util.SecurityUtils;
import com.aguardi.ecommerce.user.entity.User;
import com.aguardi.ecommerce.user.repository.UserRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentWebhookLogRepository webhookLogRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentWebhookMapper webhookMapper;

    @Value("${mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.pending-url}")
    private String pendingUrl;

    @Value("${app.name}")
    private String appName;

    // ========================================
    // GESTIÓN DE PAGOS
    // ========================================

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating payment for user: {} and order: {}", userId, request.getOrderId());

        // Validar que la orden existe y pertenece al usuario
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", request.getOrderId()));

        if (!SecurityUtils.isOwnerOrAdmin(order.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para crear un pago para esta orden");
        }

        // Verificar que no existe ya un pago para esta orden
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("Ya existe un pago para esta orden");
        }

        // Crear pago
        Payment payment = Payment.builder()
                .order(order)
                .user(order.getUser())
                .amount(order.getTotal())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.MERCADOPAGO)
                .build();

        payment = paymentRepository.save(payment);

        // Crear preferencia de MercadoPago
        try {
            Preference preference = createMercadoPagoPreferenceInternal(order, payment);

            // Actualizar payment con preference ID
            payment.setPreferenceId(preference.getId());
            payment = paymentRepository.save(payment);

            log.info("Payment created successfully: {} with preference: {}",
                    payment.getId(), preference.getId());

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(order.getId())
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .message("Redirigir al usuario a initPoint para completar el pago")
                    .build();

        } catch (MPException | MPApiException e) {
            log.error("Error creating MercadoPago preference", e);
            // Eliminar el pago creado si falla la preferencia
            paymentRepository.delete(payment);
            throw new PaymentException("Error al crear la preferencia de pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long paymentId) {
        log.info("Getting payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "id", paymentId));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(payment.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver este pago");
        }

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        log.info("Getting payment by order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "orderId", orderId));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(payment.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver este pago");
        }

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByExternalId(String externalPaymentId) {
        log.info("Getting payment by external ID: {}", externalPaymentId);

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "externalId", externalPaymentId));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(payment.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver este pago");
        }

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getCurrentUserPayments(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Getting payments for user: {}", userId);

        Page<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        log.info("Getting all payments");

        // Solo admins
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden ver todos los pagos");
        }

        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.info("Getting payments by status: {}", status);

        // Solo admins
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden filtrar pagos");
        }

        Page<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return payments.map(paymentMapper::toDTO);
    }

    // ========================================
    // MERCADOPAGO
    // ========================================

    @Override
    @Transactional
    public PaymentResponse createMercadoPagoPreference(Long orderId) {
        log.info("Creating MercadoPago preference for order: {}", orderId);

        // Buscar orden
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", orderId));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(order.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para crear pago para esta orden");
        }

        // Buscar o crear pago
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    Payment newPayment = Payment.builder()
                            .order(order)
                            .user(order.getUser())
                            .amount(order.getTotal())
                            .status(PaymentStatus.PENDING)
                            .method(PaymentMethod.MERCADOPAGO)
                            .build();
                    return paymentRepository.save(newPayment);
                });

        try {
            Preference preference = createMercadoPagoPreferenceInternal(order, payment);

            payment.setPreferenceId(preference.getId());
            payment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(order.getId())
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .message("Preferencia creada exitosamente")
                    .build();

        } catch (MPException | MPApiException e) {
            log.error("Error creating MercadoPago preference", e);
            throw new PaymentException("Error al crear la preferencia de pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processMercadoPagoWebhook(Map<String, Object> webhookData) {
        log.info("Processing MercadoPago webhook: {}", webhookData);

        // Guardar log del webhook
        PaymentWebhookLog webhookLog = new PaymentWebhookLog();
        webhookLog.setAction((String) webhookData.get("action"));
        webhookLog.setProcessed(false);

        // Serializar el payload
        try {
            webhookLog.setPayload(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(webhookData));
        } catch (Exception e) {
            webhookLog.setPayload(webhookData.toString());
        }

        webhookLog = webhookLogRepository.save(webhookLog);

        try {
            // Extraer datos del webhook
            String topic = (String) webhookData.get("topic");
            String action = (String) webhookData.get("action");

            if ("payment".equals(topic) && action != null) {
                // Obtener payment ID desde el webhook
                Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
                if (data != null && data.get("id") != null) {
                    String externalPaymentId = data.get("id").toString();

                    // Buscar el pago
                    Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Pago", "externalId", externalPaymentId
                            ));

                    // Actualizar estado según el webhook
                    updatePaymentFromWebhook(payment, webhookData);

                    // Marcar webhook como procesado
                    webhookLog.setProcessed(true);
                    webhookLog.setProcessedAt(LocalDateTime.now());
                    webhookLogRepository.save(webhookLog);
                }
            }

            log.info("Webhook processed successfully: {}", webhookLog.getId());

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            webhookLog.setErrorMessage(e.getMessage());
            webhookLogRepository.save(webhookLog);
            throw new PaymentException("Error procesando webhook: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusDTO checkPaymentStatus(String externalPaymentId) {
        log.info("Checking payment status for external ID: {}", externalPaymentId);

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pago", "externalId", externalPaymentId
                ));

        return paymentMapper.toStatusDTO(payment);
    }

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    @Override
    @Transactional
    public PaymentDTO refundPayment(Long paymentId, RefundPaymentRequest request) {
        log.info("Refunding payment: {}", paymentId);

        // Solo admins pueden reembolsar
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden reembolsar pagos");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "id", paymentId));

        // Verificar que puede ser reembolsado
        if (!payment.canBeRefunded()) {
            throw new BadRequestException("Este pago no puede ser reembolsado");
        }

        // Actualizar estado
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setStatusDetail(request.getReason());

        payment = paymentRepository.save(payment);

        // Actualizar orden
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        log.info("Payment refunded successfully: {}", paymentId);

        // TODO: Llamar API de MercadoPago para reembolso real

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO cancelPayment(Long paymentId) {
        log.info("Cancelling payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "id", paymentId));

        // Verificar permisos
        if (!SecurityUtils.isOwnerOrAdmin(payment.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para cancelar este pago");
        }

        // Solo se pueden cancelar pagos pendientes
        if (!payment.isPending()) {
            throw new BadRequestException("Solo se pueden cancelar pagos pendientes");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        log.info("Payment cancelled successfully: {}", paymentId);

        return paymentMapper.toDTO(payment);
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return paymentRepository.calculateTotalRevenueByStatus(PaymentStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        return paymentRepository.calculateMonthlyRevenue(startOfMonth, endOfMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.calculateRevenueBetweenDates(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getApprovalRate() {
        Double rate = paymentRepository.calculateApprovalRate();
        return rate != null ? rate * 100 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getStalePendingPayments(int minutesOld, Pageable pageable) {
        LocalDateTime time = LocalDateTime.now().minusMinutes(minutesOld);
        List<Payment> stalePayments = paymentRepository.findStalePendingPayments(time);

        // Convertir a Page (simplificado)
        return Page.empty(pageable);
    }

    // ========================================
    // MÉTODOS PRIVADOS
    // ========================================

    /**
     * Crear preferencia en MercadoPago
     */
    private Preference createMercadoPagoPreferenceInternal(Order order, Payment payment)
            throws MPException, MPApiException {

        // Configurar MercadoPago
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

        // Crear items de la preferencia
        List<PreferenceItemRequest> items = new ArrayList<>();
        order.getItems().forEach(orderItem -> {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(orderItem.getProduct().getId().toString())
                    .title(orderItem.getProductName())
                    .quantity(orderItem.getQuantity())
                    .currencyId("ARS")
                    .unitPrice(orderItem.getProductPrice())
                    .build();
            items.add(item);
        });

        // Agregar envío como item
        if (order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            PreferenceItemRequest shippingItem = PreferenceItemRequest.builder()
                    .title("Envío")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(order.getShippingCost())
                    .build();
            items.add(shippingItem);
        }

        // Configurar URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl + "?order=" + order.getOrderNumber())
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

        // Información del pagador
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(order.getUser().getFirstName())
                .surname(order.getUser().getLastName())
                .email(order.getUser().getEmail())
                .build();

        // Crear preferencia
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(order.getOrderNumber())
                .statementDescriptor(appName)
                .expires(true)
                .expirationDateTo(OffsetDateTime.now(ZoneOffset.UTC).plusHours(24))
                .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }

    /**
     * Actualizar pago desde webhook de MercadoPago
     */
    private void updatePaymentFromWebhook(Payment payment, Map<String, Object> webhookData) {
        // Extraer estado del webhook
        String status = extractStatus(webhookData);
        String statusDetail = extractStatusDetail(webhookData);

        // Mapear estado
        PaymentStatus newStatus = mapMercadoPagoStatus(status);

        // Actualizar payment
        payment.setStatus(newStatus);
        payment.setStatusDetail(statusDetail);

        // Actualizar timestamps según el estado
        switch (newStatus) {
            case APPROVED -> {
                payment.setApprovedAt(LocalDateTime.now());
                // Actualizar orden
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                order.setPaidAt(LocalDateTime.now());
                orderRepository.save(order);
            }
            case REJECTED, CANCELLED -> {
                payment.setRejectedAt(LocalDateTime.now());
            }
        }

        paymentRepository.save(payment);
    }

    /**
     * Extraer estado del webhook
     */
    private String extractStatus(Map<String, Object> webhookData) {
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        return data != null ? (String) data.get("status") : "unknown";
    }

    /**
     * Extraer detalle del estado
     */
    private String extractStatusDetail(Map<String, Object> webhookData) {
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        return data != null ? (String) data.get("status_detail") : "";
    }

    /**
     * Mapear estado de MercadoPago a nuestro enum
     */
    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "pending" -> PaymentStatus.PENDING;
            case "in_process" -> PaymentStatus.IN_PROCESS;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "charged_back" -> PaymentStatus.CHARGED_BACK;
            default -> PaymentStatus.PENDING;
        };
    }
}