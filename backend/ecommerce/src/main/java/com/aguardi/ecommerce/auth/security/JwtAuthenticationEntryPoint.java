// ============================================
// FILE: src/main/java/com/aguardi/auth/security/JwtAuthenticationEntryPoint.java
// Propósito: Manejar errores de autenticación (cuando falta o es inválido el token)
// ============================================

package com.aguardi.ecommerce.auth.security;

import com.aguardi.ecommerce.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Unauthorized error: {}", authException.getMessage());

        // Crear respuesta de error
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Acceso no autorizado. Por favor, inicie sesión.")
                .error("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        // Configurar respuesta HTTP
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Escribir JSON de respuesta
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}