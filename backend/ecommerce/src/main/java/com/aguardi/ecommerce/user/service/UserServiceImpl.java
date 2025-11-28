// ============================================
// FILE: src/main/java/com/aguardi/user/service/UserServiceImpl.java
// Propósito: Implementación del servicio de usuarios
// ============================================

package com.aguardi.ecommerce.user.service;

import com.aguardi.ecommerce.shared.exception.BadRequestException;
import com.aguardi.ecommerce.shared.exception.ConflictException;
import com.aguardi.ecommerce.shared.exception.ForbiddenException;
import com.aguardi.ecommerce.shared.exception.ResourceNotFoundException;
import com.aguardi.ecommerce.shared.util.SecurityUtils;
import com.aguardi.ecommerce.user.dto.*;
import com.aguardi.ecommerce.user.entity.Address;
import com.aguardi.ecommerce.user.entity.User;
import com.aguardi.ecommerce.user.mapper.AddressMapper;
import com.aguardi.ecommerce.user.mapper.UserMapper;
import com.aguardi.ecommerce.user.repository.AddressRepository;
import com.aguardi.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Getting profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        UserProfileDTO profile = userMapper.toProfileDTO(user);

        // TODO: Agregar estadísticas (total de órdenes, compras, etc.)

        return profile;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        // Verificar permisos (solo el mismo usuario o admin)
        if (!SecurityUtils.isOwnerOrAdmin(userId)) {
            throw new ForbiddenException("No tiene permisos para ver este usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination");

        // Solo admins pueden ver todos los usuarios
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden ver todos los usuarios");
        }

        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);

        // Solo admins pueden buscar usuarios
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden buscar usuarios");
        }

        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(userMapper::toDTO);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UpdateUserRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar si el email cambió y si ya existe
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Ya existe un usuario con ese email");
            }
        }

        // Actualizar datos
        userMapper.updateUserFromDTO(request, user);

        user = userRepository.save(user);

        log.info("Profile updated successfully for user: {}", userId);

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // Verificar que las nuevas contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);

        return "Contraseña actualizada exitosamente";
    }

    @Override
    @Transactional
    public UserDTO toggleUserStatus(Long userId, Boolean enabled) {
        log.info("Toggling status for user: {} to {}", userId, enabled);

        // Solo admins pueden cambiar el estado
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden cambiar el estado de usuarios");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        user.setEnabled(enabled);
        user = userRepository.save(user);

        log.info("User status toggled successfully for user: {}", userId);

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        // Verificar permisos (solo el mismo usuario o admin)
        if (!SecurityUtils.isOwnerOrAdmin(userId)) {
            throw new ForbiddenException("No tiene permisos para eliminar este usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Soft delete (deshabilitar usuario en lugar de eliminarlo)
        user.setEnabled(false);
        userRepository.save(user);

        log.info("User deleted (disabled) successfully: {}", userId);
    }

    // ========================================
    // GESTIÓN DE DIRECCIONES
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getCurrentUserAddresses() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Getting addresses for user: {}", userId);

        List<Address> addresses = addressRepository.findByUserId(userId);
        return addressMapper.toDTOList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long addressId) {
        log.info("Getting address by ID: {}", addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Verificar que la dirección pertenece al usuario
        if (!SecurityUtils.isOwnerOrAdmin(address.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para ver esta dirección");
        }

        return addressMapper.toDTO(address);
    }

    @Override
    @Transactional
    public AddressDTO createAddress(CreateAddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        // Si es la primera dirección, hacerla predeterminada
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 0) {
            address.setIsDefault(true);
        }

        // Si se marca como predeterminada, remover el flag de las demás
        if (address.getIsDefault()) {
            addressRepository.removeDefaultFlagFromUserAddresses(userId);
        }

        address = addressRepository.save(address);

        log.info("Address created successfully for user: {}", userId);

        return addressMapper.toDTO(address);
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(Long addressId, UpdateAddressRequest request) {
        log.info("Updating address: {}", addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Verificar que la dirección pertenece al usuario
        if (!SecurityUtils.isOwnerOrAdmin(address.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para modificar esta dirección");
        }

        // Actualizar datos
        addressMapper.updateEntityFromDTO(request, address);

        // Si se marca como predeterminada, remover el flag de las demás
        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.removeDefaultFlagFromUserAddresses(address.getUser().getId());
            address.setIsDefault(true);
        }

        address = addressRepository.save(address);

        log.info("Address updated successfully: {}", addressId);

        return addressMapper.toDTO(address);
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddress(Long addressId) {
        log.info("Setting default address: {}", addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Verificar que la dirección pertenece al usuario
        Long userId = SecurityUtils.getCurrentUserId();
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("No tiene permisos para modificar esta dirección");
        }

        // Remover flag de todas las direcciones del usuario
        addressRepository.removeDefaultFlagFromUserAddresses(userId);

        // Establecer esta como predeterminada
        address.setIsDefault(true);
        address = addressRepository.save(address);

        log.info("Default address set successfully: {}", addressId);

        return addressMapper.toDTO(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        log.info("Deleting address: {}", addressId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Verificar que la dirección pertenece al usuario
        if (!SecurityUtils.isOwnerOrAdmin(address.getUser().getId())) {
            throw new ForbiddenException("No tiene permisos para eliminar esta dirección");
        }

        boolean wasDefault = address.getIsDefault();
        Long userId = address.getUser().getId();

        addressRepository.delete(address);

        // Si era la predeterminada, establecer otra como predeterminada
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        log.info("Address deleted successfully: {}", addressId);
    }
}