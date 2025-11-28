// ============================================
// FILE: src/main/java/com/aguardi/config/ObjectMapperConfig.java
// Propósito: Configurar ObjectMapper para usar en toda la aplicación
// ============================================

package com.aguardi.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfig {

    /**
     * Bean de ObjectMapper personalizado
     * Configurado para manejar LocalDateTime correctamente
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Registrar módulo para Java 8 date/time API
        mapper.registerModule(new JavaTimeModule());

        // No escribir fechas como timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pretty print en desarrollo
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}