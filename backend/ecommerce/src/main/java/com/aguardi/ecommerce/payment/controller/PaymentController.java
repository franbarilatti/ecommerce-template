// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/payment/controller/PaymentController.java
// Propósito: REST Controller para gestión de pagos y webhooks de MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.controller;

import com.aguardi.ecommerce.payment.dto.*;
import com.aguardi.ecommerce.payment.entity.PaymentStatus;
import com.aguardi.ecommerce.payment.service.PaymentService;
import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import com.aguardi.ecommerce.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Endpoints para gestión de pagos con MercadoPago")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    // ========================================
    // ENDPOINTS PÚBLICOS/CLIENTE
    // ========================================

    /**
     * Crear nuevo pago y preferencia de MercadoPago
     * POST /api/payments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Crear pago",
            description = "Crea un pago y genera la preferencia de MercadoPago para proceder al checkout"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        log.info("REST request to create payment for order: {}", request.getOrderId());

        PaymentResponse response = paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Pago creado exitosamente. Redirigir al usuario a init_point",
                        response
                ));
    }

    /**
     * Crear preferencia de MercadoPago para una orden existente
     * POST /api/payments/preference/{orderId}
     */
    @PostMapping("/preference/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Crear preferencia de pago",
            description = "Genera una nueva preferencia de MercadoPago para una orden existente"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> createPreference(
            @Parameter(description = "ID de la orden")
            @PathVariable Long orderId
    ) {
        log.info("REST request to create payment preference for order: {}", orderId);

        PaymentResponse response = paymentService.createMercadoPagoPreference(orderId);

        return ResponseEntity.ok(ApiResponse.success(
                "Preferencia de pago creada exitosamente",
                response
        ));
    }

    /**
     * Obtener pago por ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Ver detalle de pago",
            description = "Obtiene el detalle completo de un pago por su ID"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @Parameter(description = "ID del pago")
            @PathVariable Long id
    ) {
        log.info("REST request to get payment by ID: {}", id);

        PaymentDTO payment = paymentService.getPaymentById(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Pago obtenido exitosamente",
                payment
        ));
    }

    /**
     * Obtener pago por ID de orden
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Ver pago por orden",
            description = "Obtiene el pago asociado a una orden específica"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByOrderId(
            @Parameter(description = "ID de la orden")
            @PathVariable Long orderId
    ) {
        log.info("REST request to get payment by order ID: {}", orderId);

        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);

        return ResponseEntity.ok(ApiResponse.success(
                "Pago obtenido exitosamente",
                payment
        ));
    }

    /**
     * Obtener mis pagos
     * GET /api/payments/my-payments
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Mis pagos",
            description = "Obtiene todos los pagos del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>> getMyPayments(
            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("REST request to get current user payments - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentDTO> paymentsPage = paymentService.getCurrentUserPayments(pageable);

        PageResponse<PaymentDTO> response = PageResponse.of(paymentsPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Pagos obtenidos exitosamente",
                response
        ));
    }

    /**
     * Verificar estado de pago
     * GET /api/payments/status/{externalPaymentId}
     */
    @GetMapping("/status/{externalPaymentId}")
    @Operation(
            summary = "Verificar estado de pago",
            description = "Consulta el estado actual de un pago en MercadoPago"
    )
    public ResponseEntity<ApiResponse<PaymentStatusDTO>> checkPaymentStatus(
            @Parameter(description = "ID del pago en MercadoPago")
            @PathVariable String externalPaymentId
    ) {
        log.info("REST request to check payment status: {}", externalPaymentId);

        PaymentStatusDTO status = paymentService.checkPaymentStatus(externalPaymentId);

        return ResponseEntity.ok(ApiResponse.success(
                "Estado de pago obtenido",
                status
        ));
    }

    /**
     * Cancelar pago pendiente
     * DELETE /api/payments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Cancelar pago",
            description = "Cancela un pago que está en estado pendiente"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> cancelPayment(
            @Parameter(description = "ID del pago")
            @PathVariable Long id
    ) {
        log.info("REST request to cancel payment: {}", id);

        PaymentDTO payment = paymentService.cancelPayment(id);

        return ResponseEntity.ok(ApiResponse.success(
                "Pago cancelado exitosamente",
                payment
        ));
    }

    // ========================================
    // WEBHOOK DE MERCADOPAGO
    // ========================================

    /**
     * Webhook de MercadoPago
     * POST /api/payments/webhook
     *
     * Este endpoint es llamado por MercadoPago cuando cambia el estado de un pago
     */
    @PostMapping("/webhook")
    @Operation(
            summary = "Webhook de MercadoPago",
            description = "Endpoint para recibir notificaciones de MercadoPago sobre cambios en pagos"
    )
    public ResponseEntity<MessageResponse> mercadoPagoWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId
    ) {
        log.info("Received MercadoPago webhook - Request ID: {}", requestId);
        log.debug("Webhook data: {}", webhookData);

        try {
            // TODO: Validar firma del webhook (opcional pero recomendado)

            paymentService.processMercadoPagoWebhook(webhookData);

            return ResponseEntity.ok(
                    MessageResponse.success("Webhook procesado exitosamente")
            );

        } catch (Exception e) {
            log.error("Error processing MercadoPago webhook", e);
            // Siempre devolver 200 a MercadoPago para evitar reintentos
            return ResponseEntity.ok(
                    MessageResponse.error("Error procesando webhook: " + e.getMessage())
            );
        }
    }

    // ========================================
    // ENDPOINTS ADMIN
    // ========================================

    /**
     * Obtener todos los pagos (Admin)
     * GET /api/payments/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar todos los pagos",
            description = "Obtiene todos los pagos del sistema"
    )
    public ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("REST request to get all payments - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentDTO> paymentsPage = paymentService.getAllPayments(pageable);

        PageResponse<PaymentDTO> response = PageResponse.of(paymentsPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Pagos obtenidos exitosamente",
                response
        ));
    }

    /**
     * Obtener pagos por estado (Admin)
     * GET /api/payments/admin/by-status/{status}
     */
    @GetMapping("/admin/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar pagos por estado",
            description = "Filtra pagos por estado específico"
    )
    public ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>> getPaymentsByStatus(
            @Parameter(description = "Estado del pago")
            @PathVariable PaymentStatus status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("REST request to get payments by status: {}", status);

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentDTO> paymentsPage = paymentService.getPaymentsByStatus(status, pageable);

        PageResponse<PaymentDTO> response = PageResponse.of(paymentsPage);

        return ResponseEntity.ok(ApiResponse.success(
                "Pagos filtrados exitosamente",
                response
        ));
    }

    /**
     * Reembolsar pago (Admin)
     * POST /api/payments/admin/{id}/refund
     */
    @PostMapping("/admin/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Reembolsar pago",
            description = "Procesa un reembolso total o parcial de un pago aprobado"
    )
    public ResponseEntity<ApiResponse<PaymentDTO>> refundPayment(
            @Parameter(description = "ID del pago")
            @PathVariable Long id,

            @Valid @RequestBody RefundPaymentRequest request
    ) {
        log.info("REST request to refund payment: {}", id);

        PaymentDTO payment = paymentService.refundPayment(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Pago reembolsado exitosamente",
                payment
        ));
    }

    /**
     * Estadísticas de pagos (Admin)
     * GET /api/payments/admin/stats
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Estadísticas de pagos",
            description = "Obtiene estadísticas generales de pagos"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {
        log.info("REST request to get payment statistics");

        BigDecimal totalRevenue = paymentService.calculateTotalRevenue();
        BigDecimal monthlyRevenue = paymentService.calculateMonthlyRevenue();
        Double approvalRate = paymentService.getApprovalRate();

        Map<String, Object> stats = Map.of(
                "totalRevenue", totalRevenue,
                "monthlyRevenue", monthlyRevenue,
                "approvalRate", approvalRate
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Estadísticas obtenidas exitosamente",
                stats
        ));
    }

    // ========================================
    // ENDPOINT DE SALUD
    // ========================================

    /**
     * Health check del módulo de pagos
     * GET /api/payments/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verifica que el módulo de pagos está funcionando"
    )
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(
                MessageResponse.success("Payment module is running")
        );
    }
}