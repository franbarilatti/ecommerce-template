// ============================================
// FILE: src/main/java/com/aguardi/user/repository/UserRepository.java
// Propósito: Repositorio de usuarios con queries personalizadas
// ============================================

package com.aguardi.ecommerce.user.repository;

import com.aguardi.ecommerce.user.entity.Role;
import com.aguardi.ecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========================================
    // BÚSQUEDAS BÁSICAS
    // ========================================

    /**
     * Buscar usuario por email
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verificar si existe un usuario con ese email
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Buscar usuario por email (ignorando mayúsculas/minúsculas)
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmailIgnoreCase(String email);

    // ========================================
    // BÚSQUEDAS POR ROL
    // ========================================

    /**
     * Buscar usuarios por rol
     * @param role Rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<User> findByRole(Role role);

    /**
     * Buscar usuarios por rol con paginación
     * @param role Rol a buscar
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Contar usuarios por rol
     * @param role Rol a contar
     * @return Cantidad de usuarios
     */
    long countByRole(Role role);

    // ========================================
    // BÚSQUEDAS POR ESTADO
    // ========================================

    /**
     * Buscar usuarios activos
     * @param pageable Configuración de paginación
     * @return Página de usuarios activos
     */
    Page<User> findByEnabledTrue(Pageable pageable);

    /**
     * Buscar usuarios inactivos
     * @param pageable Configuración de paginación
     * @return Página de usuarios inactivos
     */
    Page<User> findByEnabledFalse(Pageable pageable);

    /**
     * Buscar usuarios con email verificado
     * @return Lista de usuarios verificados
     */
    List<User> findByEmailVerifiedTrue();

    /**
     * Buscar usuarios con email sin verificar
     * @return Lista de usuarios sin verificar
     */
    List<User> findByEmailVerifiedFalse();

    // ========================================
    // BÚSQUEDAS POR NOMBRE
    // ========================================

    /**
     * Buscar usuarios por nombre (ignorando mayúsculas)
     * @param firstName Nombre a buscar
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    /**
     * Buscar usuarios por apellido (ignorando mayúsculas)
     * @param lastName Apellido a buscar
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    /**
     * Buscar usuarios por nombre completo usando query personalizada
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========================================
    // BÚSQUEDAS POR FECHA
    // ========================================

    /**
     * Buscar usuarios creados después de una fecha
     * @param date Fecha a partir de la cual buscar
     * @return Lista de usuarios
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Buscar usuarios creados entre dos fechas
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Lista de usuarios
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar usuarios que iniciaron sesión recientemente
     * @param date Fecha a partir de la cual buscar
     * @return Lista de usuarios
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :date ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyActiveUsers(@Param("date") LocalDateTime date);

    // ========================================
    // QUERIES ESTADÍSTICAS
    // ========================================

    /**
     * Contar total de usuarios
     * @return Total de usuarios
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    /**
     * Contar usuarios activos
     * @return Total de usuarios activos
     */
    long countByEnabledTrue();

    /**
     * Contar usuarios registrados en el último mes
     * @param date Fecha de hace un mes
     * @return Cantidad de usuarios nuevos
     */
    long countByCreatedAtAfter(LocalDateTime date);

    // ========================================
    // ACTUALIZACIONES
    // ========================================

    /**
     * Actualizar última fecha de login
     * @param userId ID del usuario
     * @param lastLoginAt Nueva fecha de login
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * Habilitar/deshabilitar usuario
     * @param userId ID del usuario
     * @param enabled Estado a establecer
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Marcar email como verificado
     * @param userId ID del usuario
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") Long userId);
}

