// ============================================
// FILE: src/main/java/com/aguardi/shared/dto/UploadResponse.java
// Propósito: DTO para respuesta de upload de archivos
// ============================================

package com.aguardi.ecommerce.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {

    private String url;

    private String publicId; // Para Cloudinary

    private String fileName;

    private Long fileSize;

    private String contentType;

    private String message;
}