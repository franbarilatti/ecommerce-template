// ============================================
// FILE: src/main/java/com/aguardi/user/service/UserService.java
// Propósito: Interface del servicio de usuarios
// ============================================

package com.aguardi.ecommerce.user.service;

import com.aguardi.ecommerce.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    /**
     * Obtener perfil del usuario autenticado
     * @return Perfil completo del usuario
     */
    UserProfileDTO getCurrentUserProfile();

    /**
     * Obtener usuario por ID
     * @param userId ID del usuario
     * @return DTO del usuario
     */
    UserDTO getUserById(Long userId);

    /**
     * Obtener todos los usuarios (solo admin)
     * @param pageable Paginación
     * @return Página de usuarios
     */
    Page<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Buscar usuarios por término
     * @param searchTerm Término de búsqueda
     * @param pageable Paginación
     * @return Página de usuarios
     */
    Page<UserDTO> searchUsers(String searchTerm, Pageable pageable);

    /**
     * Actualizar perfil del usuario
     * @param request Datos a actualizar
     * @return Usuario actualizado
     */
    UserDTO updateProfile(UpdateUserRequest request);

    /**
     * Cambiar contraseña
     * @param request Contraseñas actual y nueva
     * @return Mensaje de confirmación
     */
    String changePassword(ChangePasswordRequest request);

    /**
     * Habilitar/deshabilitar usuario (solo admin)
     * @param userId ID del usuario
     * @param enabled Estado a establecer
     * @return Usuario actualizado
     */
    UserDTO toggleUserStatus(Long userId, Boolean enabled);

    /**
     * Eliminar usuario (soft delete)
     * @param userId ID del usuario
     */
    void deleteUser(Long userId);

    // ========================================
    // GESTIÓN DE DIRECCIONES
    // ========================================

    /**
     * Obtener direcciones del usuario autenticado
     * @return Lista de direcciones
     */
    List<AddressDTO> getCurrentUserAddresses();

    /**
     * Obtener dirección por ID
     * @param addressId ID de la dirección
     * @return DTO de la dirección
     */
    AddressDTO getAddressById(Long addressId);

    /**
     * Crear nueva dirección
     * @param request Datos de la dirección
     * @return Dirección creada
     */
    AddressDTO createAddress(CreateAddressRequest request);

    /**
     * Actualizar dirección
     * @param addressId ID de la dirección
     * @param request Datos a actualizar
     * @return Dirección actualizada
     */
    AddressDTO updateAddress(Long addressId, UpdateAddressRequest request);

    /**
     * Establecer dirección como predeterminada
     * @param addressId ID de la dirección
     * @return Dirección actualizada
     */
    AddressDTO setDefaultAddress(Long addressId);

    /**
     * Eliminar dirección
     * @param addressId ID de la dirección
     */
    void deleteAddress(Long addressId);
}