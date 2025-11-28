// ============================================
// FILE: src/main/java/com/aguardi/auth/controller/AuthController.java
// Propósito: Controller para endpoints de autenticación
// ============================================

package com.aguardi.ecommerce.auth.controller;

import com.aguardi.ecommerce.auth.dto.*;
import com.aguardi.ecommerce.auth.service.AuthService;
import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final AuthService authService;

    /**
     * Login de usuario
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autenticar usuario con email y contraseña, retorna token JWT"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login exitoso", response)
        );
    }

    /**
     * Registro de nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crear cuenta de usuario nueva con rol CLIENT"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email ya existe"
            )
    })
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request received for: {}", request.getEmail());

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente", response));
    }

    /**
     * Refrescar token JWT
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar token JWT",
            description = "Obtener nuevo access token usando refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado"
            )
    })
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Refresh token request received");

        RefreshTokenResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("Token refrescado exitosamente", response)
        );
    }

    /**
     * Verificar email
     * GET /api/auth/verify-email?token={token}
     */
    @GetMapping("/verify-email")
    @Operation(
            summary = "Verificar email",
            description = "Verificar email del usuario con token enviado por correo"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verificado exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o expirado"
            )
    })
    public ResponseEntity<ApiResponse<MessageResponse>> verifyEmail(
            @RequestParam String token) {

        log.info("Email verification request received");

        String message = authService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }

    /**
     * Solicitar recuperación de contraseña
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Recuperar contraseña",
            description = "Enviar email con link para resetear contraseña"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email enviado exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Forgot password request received for: {}", request.getEmail());

        String message = authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }

    /**
     * Resetear contraseña con token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Resetear contraseña",
            description = "Establecer nueva contraseña usando token de recuperación"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o contraseñas no coinciden"
            )
    })
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Reset password request received");

        String message = authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }

    /**
     * Logout (opcional - solo invalida token en frontend)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Endpoint informativo - JWT debe ser eliminado del cliente"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso"
            )
    })
    public ResponseEntity<ApiResponse<MessageResponse>> logout() {

        log.info("Logout request received");

        // Con JWT stateless, el logout se maneja en el frontend eliminando el token
        // Opcionalmente, se puede implementar una blacklist de tokens

        String message = "Sesión cerrada exitosamente. Por favor, elimine el token del cliente.";

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }

    /**
     * Health check del servicio de autenticación
     * GET /api/auth/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verificar que el servicio de autenticación está funcionando"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Auth service is running",
                        MessageResponse.success("OK")
                )
        );
    }
}