// ============================================
// FILE: src/main/java/com/aguardi/config/SecurityConfig.java
// Propósito: Configuración principal de Spring Security
// ============================================

package com.aguardi.ecommerce.config;

import com.aguardi.ecommerce.auth.security.CustomUserDetailsService;
import com.aguardi.ecommerce.auth.security.JwtAuthenticationEntryPoint;
import com.aguardi.ecommerce.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configurar cadena de filtros de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario para API REST con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar autorización de requests
                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // ENDPOINTS PÚBLICOS (sin autenticación)
                        // ========================================

                        // Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Productos (lectura pública)
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Actuator (health checks)
                        .requestMatchers("/api/actuator/health/**").permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/api/docs/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html"
                        ).permitAll()

                        // MercadoPago webhooks
                        .requestMatchers("/api/payments/webhook/**").permitAll()

                        // ========================================
                        // ENDPOINTS DE ADMIN
                        // ========================================

                        // Gestión de productos
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Gestión de categorías
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        // Gestión de usuarios
                        .requestMatchers("/api/admin/users/**").hasRole("ADMIN")

                        // Gestión de órdenes (actualizar estado)
                        .requestMatchers(HttpMethod.PUT, "/api/admin/orders/**").hasRole("ADMIN")

                        // Dashboard y estadísticas
                        .requestMatchers("/api/admin/dashboard/**").hasRole("ADMIN")

                        // ========================================
                        // ENDPOINTS AUTENTICADOS (cualquier usuario logueado)
                        // ========================================

                        // Perfil de usuario
                        .requestMatchers("/api/users/me/**").authenticated()

                        // Direcciones
                        .requestMatchers("/api/users/addresses/**").authenticated()

                        // Órdenes (crear y ver propias)
                        .requestMatchers("/api/orders/**").authenticated()

                        // Pagos
                        .requestMatchers("/api/payments/**").authenticated()

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // Manejo de excepciones de autenticación
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Política de sesiones: STATELESS (no mantener sesión en servidor)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar authentication provider
                .authenticationProvider(authenticationProvider())

                // Agregar filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configurar CORS
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration =
                new org.springframework.web.cors.CorsConfiguration();

        // Permitir orígenes desde application.yml
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(java.util.Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(java.util.List.of("*"));

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Tiempo de cache de preflight requests
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configurar authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Bean para el password encoder (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para el authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
