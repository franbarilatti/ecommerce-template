// ============================================
// FILE: src/main/java/com/aguardi/user/mapper/UserMapper.java
// Propósito: Mapper para convertir entre User Entity y DTOs
// ============================================

package com.aguardi.ecommerce.user.mapper;

import com.aguardi.ecommerce.user.dto.*;
import com.aguardi.ecommerce.user.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir User Entity a UserDTO
     * @param user Entidad de usuario
     * @return DTO de usuario
     */
    UserDTO toDTO(User user);

    /**
     * Convertir lista de User a lista de UserDTO
     * @param users Lista de entidades
     * @return Lista de DTOs
     */
    List<UserDTO> toDTOList(List<User> users);

    /**
     * Convertir User Entity a UserProfileDTO (con direcciones)
     * @param user Entidad de usuario
     * @return DTO de perfil completo
     */
    @Mapping(target = "addresses", source = "addresses")
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalPurchases", ignore = true)
    UserProfileDTO toProfileDTO(User user);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir UpdateUserRequest a User Entity (para actualizar)
     * @param request Request con datos a actualizar
     * @param user Entidad existente a actualizar
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UpdateUserRequest request, @MappingTarget User user);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Método por defecto para calcular nombre completo
     * Se puede usar con @AfterMapping si es necesario
     */
    @AfterMapping
    default void enrichUserDTO(@MappingTarget UserDTO dto, User user) {
        // Aquí se pueden agregar enriquecimientos adicionales si es necesario
    }
}