// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/notification/service/EmailServiceImpl.java
// Propósito: Implementación del servicio de emails con Thymeleaf
// ============================================

package com.aguardi.ecommerce.notification.service;

import com.aguardi.ecommerce.order.entity.Order;
import com.aguardi.ecommerce.shared.exception.EmailException;
import com.aguardi.ecommerce.user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.support-email}")
    private String supportEmail;

    // ========================================
    // EMAILS ESPECÍFICOS
    // ========================================

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getFirstName());
            variables.put("appName", appName);
            variables.put("loginUrl", frontendUrl + "/login.html");
            variables.put("catalogUrl", frontendUrl + "/catalog.html");
            variables.put("supportEmail", supportEmail);

            sendTemplateEmail(
                    user.getEmail(),
                    "¡Bienvenido a " + appName + "!",
                    "welcome-email",
                    variables
            );

            log.info("Welcome email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", user.getEmail(), e);
            // No lanzar excepción para no interrumpir el flujo de registro
        }
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        log.info("Sending order confirmation email for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", order.getUser().getFirstName());
            variables.put("orderNumber", order.getOrderNumber());
            variables.put("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            variables.put("items", order.getItems());
            variables.put("subtotal", order.getSubtotal());
            variables.put("shippingCost", order.getShippingCost());
            variables.put("discount", order.getDiscount());
            variables.put("total", order.getTotal());
            variables.put("shippingAddress", formatShippingAddress(order));
            variables.put("orderDetailsUrl", frontendUrl + "/profile.html#orders");
            variables.put("appName", appName);
            variables.put("supportEmail", supportEmail);

            sendTemplateEmail(
                    order.getUser().getEmail(),
                    "Confirmación de Orden #" + order.getOrderNumber(),
                    "order-confirmation",
                    variables
            );

            log.info("Order confirmation email sent successfully for: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Error sending order confirmation email for: {}", order.getOrderNumber(), e);
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        log.info("Sending order status update email for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", order.getUser().getFirstName());
            variables.put("orderNumber", order.getOrderNumber());
            variables.put("status", translateOrderStatus(order.getStatus().name()));
            variables.put("statusMessage", getStatusMessage(order.getStatus().name()));
            variables.put("trackingNumber", order.getShippingInfo() != null
                    ? order.getShippingInfo().getTrackingNumber()
                    : null);
            variables.put("carrier", order.getShippingInfo() != null
                    ? order.getShippingInfo().getCarrier()
                    : null);
            variables.put("orderDetailsUrl", frontendUrl + "/profile.html#orders");
            variables.put("appName", appName);
            variables.put("supportEmail", supportEmail);

            sendTemplateEmail(
                    order.getUser().getEmail(),
                    "Actualización de Orden #" + order.getOrderNumber(),
                    "order-status-update",
                    variables
            );

            log.info("Order status update email sent successfully for: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Error sending order status update email for: {}", order.getOrderNumber(), e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        try {
            String resetUrl = frontendUrl + "/reset-password.html?token=" + resetToken;

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getFirstName());
            variables.put("resetUrl", resetUrl);
            variables.put("appName", appName);
            variables.put("supportEmail", supportEmail);
            variables.put("expirationHours", 24); // Token válido por 24 horas

            sendTemplateEmail(
                    user.getEmail(),
                    "Restablecer Contraseña - " + appName,
                    "password-reset",
                    variables
            );

            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", user.getEmail(), e);
            throw new EmailException("Error al enviar email de reseteo de contraseña");
        }
    }

    // ========================================
    // MÉTODOS GENÉRICOS
    // ========================================

    @Override
    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        log.info("Sending email to: {} with subject: {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Error sending email to: {}", to, e);
            throw new EmailException("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Object variables) {
        log.info("Sending template email: {} to: {}", templateName, to);

        try {
            Context context = new Context(Locale.forLanguageTag("es-AR"));

            // Agregar variables al contexto
            if (variables instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> variablesMap = (Map<String, Object>) variables;
                variablesMap.forEach(context::setVariable);
            }

            // Procesar template
            String htmlContent = templateEngine.process(templateName, context);

            // Enviar email
            sendEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Error processing email template: {}", templateName, e);
            throw new EmailException("Error al procesar template de email: " + e.getMessage());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Formatear dirección de envío
     */
    private String formatShippingAddress(Order order) {
        if (order.getShippingInfo() == null) {
            return "Dirección no especificada";
        }

        var shipping = order.getShippingInfo();
        return String.format("%s, %s, %s - CP: %s",
                shipping.getStreet(),
                shipping.getCity(),
                shipping.getProvince(),
                shipping.getPostalCode()
        );
    }

    /**
     * Traducir estado de orden a español
     */
    private String translateOrderStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Pendiente";
            case "PAID" -> "Pagada";
            case "PROCESSING" -> "En Proceso";
            case "SHIPPED" -> "Enviada";
            case "DELIVERED" -> "Entregada";
            case "CANCELLED" -> "Cancelada";
            case "REFUNDED" -> "Reembolsada";
            default -> status;
        };
    }

    /**
     * Obtener mensaje descriptivo según el estado
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Tu orden ha sido registrada y está pendiente de pago.";
            case "PAID" -> "¡Tu pago ha sido confirmado! Estamos preparando tu pedido.";
            case "PROCESSING" -> "Tu pedido está siendo preparado para el envío.";
            case "SHIPPED" -> "¡Tu pedido ya está en camino! Puedes hacer seguimiento con el número de tracking.";
            case "DELIVERED" -> "¡Tu pedido ha sido entregado! Esperamos que lo disfrutes.";
            case "CANCELLED" -> "Tu orden ha sido cancelada. Si tienes dudas, contáctanos.";
            case "REFUNDED" -> "Tu orden ha sido reembolsada. El dinero será devuelto según el método de pago usado.";
            default -> "El estado de tu orden ha sido actualizado.";
        };
    }
}