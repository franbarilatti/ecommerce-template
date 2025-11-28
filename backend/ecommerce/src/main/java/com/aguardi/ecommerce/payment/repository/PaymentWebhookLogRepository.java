// ============================================
// FILE: src/main/java/com/aguardi/payment/repository/PaymentWebhookLogRepository.java
// Propósito: Repositorio de logs de webhooks de MercadoPago
// ============================================

package com.aguardi.ecommerce.payment.repository;

import com.aguardi.ecommerce.payment.entity.PaymentWebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, Long> {

    /**
     * Buscar logs por ID de pago externo
     * @param externalPaymentId ID del pago en MercadoPago
     * @return Lista de logs para ese pago
     */
    List<PaymentWebhookLog> findByExternalPaymentIdOrderByCreatedAtDesc(String externalPaymentId);

    /**
     * Buscar logs no procesados
     * @param pageable Configuración de paginación
     * @return Página de logs sin procesar
     */
    Page<PaymentWebhookLog> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);

    /**
     * Buscar logs por acción
     * @param action Tipo de acción (payment.created, payment.updated, etc.)
     * @param pageable Configuración de paginación
     * @return Página de logs
     */
    Page<PaymentWebhookLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * Buscar logs con errores
     * @param pageable Configuración de paginación
     * @return Página de logs con errores
     */
    @Query("SELECT wl FROM PaymentWebhookLog wl WHERE wl.errorMessage IS NOT NULL ORDER BY wl.createdAt DESC")
    Page<PaymentWebhookLog> findLogsWithErrors(Pageable pageable);

    /**
     * Buscar logs antiguos (para limpieza)
     * @param date Fecha límite
     * @return Lista de logs antiguos
     */
    List<PaymentWebhookLog> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Contar webhooks no procesados
     * @return Total de webhooks sin procesar
     */
    long countByProcessedFalse();

    /**
     * Marcar webhook como procesado
     * @param webhookId ID del webhook log
     * @param processedAt Fecha y hora de procesamiento
     */
    @Modifying
    @Query("UPDATE PaymentWebhookLog wl SET wl.processed = true, wl.processedAt = :processedAt WHERE wl.id = :webhookId")
    void markAsProcessed(@Param("webhookId") Long webhookId, @Param("processedAt") LocalDateTime processedAt);

    /**
     * Registrar error en webhook
     * @param webhookId ID del webhook log
     * @param errorMessage Mensaje de error
     */
    @Modifying
    @Query("UPDATE PaymentWebhookLog wl SET wl.errorMessage = :errorMessage WHERE wl.id = :webhookId")
    void registerError(@Param("webhookId") Long webhookId, @Param("errorMessage") String errorMessage);

    /**
     * Eliminar logs antiguos (limpieza)
     * @param date Fecha límite
     */
    @Modifying
    void deleteByCreatedAtBeforeAndProcessedTrue(LocalDateTime date);
}
