// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/notification/service/EmailService.java
// Propósito: Interface para envío de emails
// ============================================

package com.aguardi.ecommerce.notification.service;

import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.user.entity.User;

public interface EmailService {

    /**
     * Enviar email de bienvenida al registrarse
     * @param user Usuario registrado
     */
    void sendWelcomeEmail(User user);

    /**
     * Enviar email de confirmación de orden
     * @param order Orden creada
     */
    void sendOrderConfirmationEmail(Order order);

    /**
     * Enviar email de actualización de estado de orden
     * @param order Orden actualizada
     */
    void sendOrderStatusUpdateEmail(Order order);

    /**
     * Enviar email de reseteo de contraseña
     * @param user Usuario que solicitó reseteo
     * @param resetToken Token de reseteo
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Enviar email genérico
     * @param to Email destino
     * @param subject Asunto
     * @param htmlContent Contenido HTML
     */
    void sendEmail(String to, String subject, String htmlContent);

    /**
     * Enviar email con template
     * @param to Email destino
     * @param subject Asunto
     * @param templateName Nombre del template (sin extensión)
     * @param variables Variables para el template
     */
    void sendTemplateEmail(String to, String subject, String templateName, Object variables);
}