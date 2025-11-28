// ============================================
// FILE: src/main/java/com/aguardi/user/controller/UserController.java
// Propósito: Controller para endpoints de usuarios
// ============================================

package com.aguardi.ecommerce.user.controller;

import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.IdResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import com.aguardi.ecommerce.shared.dto.PageResponse;
import com.aguardi.ecommerce.user.dto.*;
import com.aguardi.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints de gestión de usuarios")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    // ========================================
    // ENDPOINTS DE PERFIL DE USUARIO
    // ========================================

    /**
     * Obtener perfil del usuario autenticado
     * GET /api/users/me
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obtener perfil actual",
            description = "Obtener perfil completo del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        log.info("Get current user profile request received");

        UserProfileDTO profile = userService.getCurrentUserProfile();

        return ResponseEntity.ok(
                ApiResponse.success(profile)
        );
    }

    /**
     * Actualizar perfil del usuario
     * PUT /api/users/me
     */
    @PutMapping("/me")
    @Operation(
            summary = "Actualizar perfil",
            description = "Actualizar información del perfil del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Update profile request received");

        UserDTO user = userService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success("Perfil actualizado exitosamente", user)
        );
    }

    /**
     * Cambiar contraseña
     * POST /api/users/me/change-password
     */
    @PostMapping("/me/change-password")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Cambiar la contraseña del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request received");

        String message = userService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(message, MessageResponse.success(message))
        );
    }

    // ========================================
    // ENDPOINTS DE ADMINISTRACIÓN DE USUARIOS
    // ========================================

    /**
     * Obtener todos los usuarios (solo admin)
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar usuarios",
            description = "Obtener lista paginada de todos los usuarios (solo admin)"
    )
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get all users request received - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> users = userService.getAllUsers(pageable);
        PageResponse<UserDTO> pageResponse = PageResponse.of(users);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Buscar usuarios (solo admin)
     * GET /api/users/search?q=term
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar usuarios",
            description = "Buscar usuarios por nombre o email (solo admin)"
    )
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Search users request received - query: {}", q);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.searchUsers(q, pageable);
        PageResponse<UserDTO> pageResponse = PageResponse.of(users);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse)
        );
    }

    /**
     * Obtener usuario por ID (admin o mismo usuario)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtener información de un usuario específico"
    )
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long id) {

        log.info("Get user by ID request received - id: {}", id);

        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(user)
        );
    }

    /**
     * Habilitar/deshabilitar usuario (solo admin)
     * PATCH /api/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cambiar estado de usuario",
            description = "Habilitar o deshabilitar un usuario (solo admin)"
    )
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {

        log.info("Toggle user status request received - id: {}, enabled: {}", id, enabled);

        UserDTO user = userService.toggleUserStatus(id, enabled);

        String message = enabled ? "Usuario habilitado exitosamente" : "Usuario deshabilitado exitosamente";

        return ResponseEntity.ok(
                ApiResponse.success(message, user)
        );
    }

    /**
     * Eliminar usuario (soft delete)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Eliminar (deshabilitar) un usuario"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> deleteUser(
            @PathVariable Long id) {

        log.info("Delete user request received - id: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Usuario eliminado exitosamente",
                        MessageResponse.success("Usuario eliminado exitosamente")
                )
        );
    }

    // ========================================
    // ENDPOINTS DE DIRECCIONES
    // ========================================

    /**
     * Obtener direcciones del usuario autenticado
     * GET /api/users/me/addresses
     */
    @GetMapping("/me/addresses")
    @Operation(
            summary = "Listar direcciones",
            description = "Obtener todas las direcciones del usuario autenticado"
    )
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getCurrentUserAddresses() {
        log.info("Get current user addresses request received");

        List<AddressDTO> addresses = userService.getCurrentUserAddresses();

        return ResponseEntity.ok(
                ApiResponse.success(addresses)
        );
    }

    /**
     * Obtener dirección por ID
     * GET /api/users/addresses/{id}
     */
    @GetMapping("/addresses/{id}")
    @Operation(
            summary = "Obtener dirección por ID",
            description = "Obtener información de una dirección específica"
    )
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById(
            @PathVariable Long id) {

        log.info("Get address by ID request received - id: {}", id);

        AddressDTO address = userService.getAddressById(id);

        return ResponseEntity.ok(
                ApiResponse.success(address)
        );
    }

    /**
     * Crear nueva dirección
     * POST /api/users/me/addresses
     */
    @PostMapping("/me/addresses")
    @Operation(
            summary = "Crear dirección",
            description = "Crear una nueva dirección de envío"
    )
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {

        log.info("Create address request received");

        AddressDTO address = userService.createAddress(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dirección creada exitosamente", address));
    }

    /**
     * Actualizar dirección
     * PUT /api/users/addresses/{id}
     */
    @PutMapping("/addresses/{id}")
    @Operation(
            summary = "Actualizar dirección",
            description = "Actualizar una dirección existente"
    )
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request) {

        log.info("Update address request received - id: {}", id);

        AddressDTO address = userService.updateAddress(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Dirección actualizada exitosamente", address)
        );
    }

    /**
     * Establecer dirección como predeterminada
     * PATCH /api/users/addresses/{id}/default
     */
    @PatchMapping("/addresses/{id}/default")
    @Operation(
            summary = "Establecer dirección predeterminada",
            description = "Marcar una dirección como predeterminada para envíos"
    )
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable Long id) {

        log.info("Set default address request received - id: {}", id);

        AddressDTO address = userService.setDefaultAddress(id);

        return ResponseEntity.ok(
                ApiResponse.success("Dirección establecida como predeterminada", address)
        );
    }

    /**
     * Eliminar dirección
     * DELETE /api/users/addresses/{id}
     */
    @DeleteMapping("/addresses/{id}")
    @Operation(
            summary = "Eliminar dirección",
            description = "Eliminar una dirección de envío"
    )
    public ResponseEntity<ApiResponse<MessageResponse>> deleteAddress(
            @PathVariable Long id) {

        log.info("Delete address request received - id: {}", id);

        userService.deleteAddress(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dirección eliminada exitosamente",
                        MessageResponse.success("Dirección eliminada exitosamente")
                )
        );
    }
}