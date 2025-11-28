// ============================================
// FILE: src/main/java/com/aguardi/user/mapper/AddressMapper.java
// Propósito: Mapper para convertir entre Address Entity y DTOs
// ============================================

package com.aguardi.ecommerce.user.mapper;

import com.aguardi.ecommerce.user.dto.AddressDTO;
import com.aguardi.ecommerce.user.dto.CreateAddressRequest;
import com.aguardi.ecommerce.user.dto.UpdateAddressRequest;
import com.aguardi.ecommerce.user.entity.Address;
import com.aguardi.ecommerce.user.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {

    // ========================================
    // Entity -> DTO
    // ========================================

    /**
     * Convertir Address Entity a AddressDTO
     * @param address Entidad de dirección
     * @return DTO de dirección
     */
    @Mapping(target = "userId", source = "user.id")
    AddressDTO toDTO(Address address);

    /**
     * Convertir lista de Address a lista de AddressDTO
     * @param addresses Lista de entidades
     * @return Lista de DTOs
     */
    List<AddressDTO> toDTOList(List<Address> addresses);

    // ========================================
    // DTO -> Entity
    // ========================================

    /**
     * Convertir CreateAddressRequest a Address Entity
     * @param request Request con datos de nueva dirección
     * @return Nueva entidad de dirección
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toEntity(CreateAddressRequest request);

    /**
     * Actualizar Address Entity desde UpdateAddressRequest
     * @param request Request con datos a actualizar
     * @param address Entidad existente a actualizar
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(UpdateAddressRequest request, @MappingTarget Address address);

    // ========================================
    // Métodos por defecto (helpers)
    // ========================================

    /**
     * Método para asociar dirección a un usuario
     * @param address Dirección a asociar
     * @param user Usuario propietario
     */
    default void setUser(Address address, User user) {
        if (address != null && user != null) {
            address.setUser(user);
        }
    }
}