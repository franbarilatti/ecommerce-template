// ============================================
// FILE: src/main/java/com/aguardi/auth/security/UserPrincipal.java
// Propósito: Clase que representa al usuario autenticado en Spring Security
// ============================================

package com.aguardi.ecommerce.auth.security;

import com.aguardi.ecommerce.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;

    /**
     * Crear UserPrincipal desde User Entity
     * @param user Entidad de usuario
     * @return UserPrincipal
     */
    public static UserPrincipal create(User user) {
        // Convertir el rol del usuario a GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority),
                user.getEnabled()
        );
    }

    @Override
    public String getUsername() {
        return email; // Usamos email como username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}