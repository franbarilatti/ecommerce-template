// ============================================
// FILE: src/main/java/com/aguardi/shared/util/SecurityUtils.java
// Propósito: Utilidades para obtener información del usuario autenticado
// ============================================

package com.aguardi.ecommerce.shared.util;

import com.aguardi.ecommerce.auth.security.UserPrincipal;
import com.aguardi.ecommerce.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Obtener el usuario autenticado actual
     * @return UserPrincipal del usuario autenticado
     * @throws UnauthorizedException si no hay usuario autenticado
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        throw new UnauthorizedException("Usuario no autenticado correctamente");
    }

    /**
     * Obtener el ID del usuario autenticado
     * @return ID del usuario
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Obtener el email del usuario autenticado
     * @return Email del usuario
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Verificar si el usuario actual es admin
     * @return true si es admin, false si no
     */
    public static boolean isAdmin() {
        try {
            UserPrincipal user = getCurrentUser();
            return user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verificar si el usuario actual es el propietario del recurso
     * @param resourceOwnerId ID del propietario del recurso
     * @return true si es el propietario o es admin
     */
    public static boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (isAdmin()) {
            return true;
        }

        try {
            return getCurrentUserId().equals(resourceOwnerId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verificar si hay un usuario autenticado
     * @return true si hay usuario autenticado, false si no
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null &&
                    authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal());
        } catch (Exception e) {
            return false;
        }
    }
}