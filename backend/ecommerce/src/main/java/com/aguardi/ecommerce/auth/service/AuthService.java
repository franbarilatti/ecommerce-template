// ============================================
// FILE: src/main/java/com/aguardi/auth/service/AuthService.java
// Propósito: Interface del servicio de autenticación
// ============================================

package com.aguardi.ecommerce.auth.service;

import com.aguardi.ecommerce.auth.dto.*;

public interface AuthService {

    /**
     * Realizar login de usuario
     * @param request Credenciales de login
     * @return Respuesta con token y datos del usuario
     */
    LoginResponse login(LoginRequest request);

    /**
     * Registrar nuevo usuario
     * @param request Datos de registro
     * @return Respuesta con token y datos del usuario
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Refrescar token JWT
     * @param request Request con refresh token
     * @return Nuevo access token
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Verificar email del usuario
     * @param token Token de verificación
     * @return Mensaje de confirmación
     */
    String verifyEmail(String token);

    /**
     * Solicitar recuperación de contraseña
     * @param request Email del usuario
     * @return Mensaje de confirmación
     */
    String forgotPassword(ForgotPasswordRequest request);

    /**
     * Resetear contraseña con token
     * @param request Token y nueva contraseña
     * @return Mensaje de confirmación
     */
    String resetPassword(ResetPasswordRequest request);
}
