// ============================================
// FILE: src/main/java/com/aguardi/auth/service/AuthServiceImpl.java
// Propósito: Implementación del servicio de autenticación
// ============================================

package com.aguardi.ecommerce.auth.service;

import com.aguardi.ecommerce.auth.dto.*;
import com.aguardi.ecommerce.auth.security.JwtTokenProvider;
import com.aguardi.ecommerce.shared.exception.BadRequestException;
import com.aguardi.ecommerce.shared.exception.ConflictException;
import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import com.aguardi.ecommerce.shared.exception.UnauthorizedException;
import com.aguardi.ecommerce.user.entity.Role;
import com.aguardi.ecommerce.user.entity.User;
import com.aguardi.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Buscar usuario en BD
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con email: " + request.getEmail()
                    ));

            // Verificar que el usuario esté habilitado
            if (!user.getEnabled()) {
                throw new UnauthorizedException("La cuenta está deshabilitada");
            }

            // Actualizar último login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generar token JWT
            String token = jwtTokenProvider.generateToken(authentication);

            log.info("User logged in successfully: {}", user.getEmail());

            // Construir respuesta
            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .build();

        } catch (AuthenticationException ex) {
            log.error("Login failed for user: {}", request.getEmail());
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.getEmail());

        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        // Verificar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CLIENT) // Por defecto es cliente
                .enabled(true)
                .emailVerified(false) // Requerirá verificación después
                .build();

        // Guardar en BD
        user = userRepository.save(user);

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("User registered successfully: {}", user.getEmail());

        // TODO: Enviar email de verificación

        // Construir respuesta
        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .message("Usuario registrado exitosamente. Por favor, verifica tu email.")
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Attempting to refresh token");

        String refreshToken = request.getRefreshToken();

        // Validar refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        // Obtener user ID del refresh token
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Buscar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar que esté habilitado
        if (!user.getEnabled()) {
            throw new UnauthorizedException("La cuenta está deshabilitada");
        }

        // Generar nuevo access token
        String newToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return RefreshTokenResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {
        log.info("Attempting to verify email with token");

        // TODO: Implementar lógica de verificación de email
        // 1. Validar token
        // 2. Buscar usuario
        // 3. Marcar email como verificado
        // 4. Actualizar en BD

        throw new UnsupportedOperationException("Email verification not implemented yet");
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for: {}", request.getEmail());

        // Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con email: " + request.getEmail()
                ));

        // TODO: Implementar lógica de recuperación de contraseña
        // 1. Generar token de reset
        // 2. Guardar token en BD (tabla password_reset_tokens)
        // 3. Enviar email con link de reset

        log.info("Password reset email sent to: {}", request.getEmail());

        return "Se ha enviado un email con instrucciones para resetear tu contraseña";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        log.info("Attempting to reset password with token");

        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        // TODO: Implementar lógica de reset de contraseña
        // 1. Validar token de reset
        // 2. Buscar usuario asociado al token
        // 3. Actualizar contraseña
        // 4. Invalidar token
        // 5. Enviar email de confirmación

        throw new UnsupportedOperationException("Password reset not implemented yet");
    }
}
