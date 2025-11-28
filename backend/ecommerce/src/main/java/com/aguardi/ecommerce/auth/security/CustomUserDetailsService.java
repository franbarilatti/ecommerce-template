// ============================================
// FILE: src/main/java/com/aguardi/auth/security/CustomUserDetailsService.java
// Propósito: Cargar detalles del usuario desde la base de datos
// ============================================

package com.aguardi.ecommerce.auth.security;

import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import com.aguardi.ecommerce.user.entity.User;
import com.aguardi.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Cargar usuario por email (usado por Spring Security en el login)
     * @param email Email del usuario
     * @return UserDetails
     * @throws UsernameNotFoundException si no se encuentra el usuario
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email
                ));

        log.debug("User loaded by email: {}", email);

        return UserPrincipal.create(user);
    }

    /**
     * Cargar usuario por ID (usado por JWT filter)
     * @param userId ID del usuario
     * @return UserDetails
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        log.debug("User loaded by ID: {}", userId);

        return UserPrincipal.create(user);
    }
}