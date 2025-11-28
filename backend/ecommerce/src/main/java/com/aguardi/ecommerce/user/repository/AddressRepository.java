// ============================================
// FILE: src/main/java/com/aguardi/user/repository/AddressRepository.java
// Propósito: Repositorio de direcciones
// ============================================

package com.aguardi.ecommerce.user.repository;

import com.aguardi.ecommerce.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // ========================================
    // BÚSQUEDAS POR USUARIO
    // ========================================

    /**
     * Buscar todas las direcciones de un usuario
     * @param userId ID del usuario
     * @return Lista de direcciones
     */
    List<Address> findByUserId(Long userId);

    /**
     * Buscar dirección por defecto de un usuario
     * @param userId ID del usuario
     * @return Optional con la dirección por defecto
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Contar direcciones de un usuario
     * @param userId ID del usuario
     * @return Cantidad de direcciones
     */
    long countByUserId(Long userId);

    // ========================================
    // BÚSQUEDAS POR UBICACIÓN
    // ========================================

    /**
     * Buscar direcciones por provincia
     * @param province Provincia
     * @return Lista de direcciones
     */
    List<Address> findByProvince(String province);

    /**
     * Buscar direcciones por ciudad
     * @param city Ciudad
     * @return Lista de direcciones
     */
    List<Address> findByCity(String city);

    /**
     * Buscar direcciones por código postal
     * @param postalCode Código postal
     * @return Lista de direcciones
     */
    List<Address> findByPostalCode(String postalCode);

    // ========================================
    // ACTUALIZACIONES
    // ========================================

    /**
     * Remover flag de dirección por defecto de todas las direcciones de un usuario
     * @param userId ID del usuario
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void removeDefaultFlagFromUserAddresses(@Param("userId") Long userId);

    /**
     * Establecer una dirección como por defecto
     * @param addressId ID de la dirección
     * @param userId ID del usuario (para seguridad)
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = true WHERE a.id = :addressId AND a.user.id = :userId")
    void setAsDefault(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * Verificar si una dirección pertenece a un usuario
     * @param addressId ID de la dirección
     * @param userId ID del usuario
     * @return true si pertenece, false si no
     */
    boolean existsByIdAndUserId(Long addressId, Long userId);
}
