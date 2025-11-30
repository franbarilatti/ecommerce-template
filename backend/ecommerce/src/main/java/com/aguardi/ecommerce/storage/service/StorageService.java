// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/storage/service/StorageService.java
// Propósito: Interface para manejo de almacenamiento de archivos
// Soporta: Local filesystem y Cloudinary
// ============================================

package com.aguardi.ecommerce.storage.service;

import com.aguardi.ecommerce.shared.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface StorageService {

    /**
     * Subir un archivo
     * @param file Archivo a subir
     * @param folder Carpeta destino (ej: "products", "categories")
     * @return Respuesta con URL del archivo
     */
    UploadResponse uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * Subir múltiples archivos
     * @param files Lista de archivos
     * @param folder Carpeta destino
     * @return Lista de respuestas con URLs
     */
    List<UploadResponse> uploadMultipleFiles(List<MultipartFile> files, String folder) throws IOException;

    /**
     * Eliminar un archivo por su ID público (Cloudinary) o nombre (Local)
     * @param publicId ID público del archivo
     * @return true si se eliminó exitosamente
     */
    boolean deleteFile(String publicId);

    /**
     * Obtener URL completa de un archivo
     * @param filename Nombre del archivo
     * @return URL completa
     */
    String getFileUrl(String filename);

    /**
     * Verificar si un archivo existe
     * @param filename Nombre del archivo
     * @return true si existe
     */
    boolean fileExists(String filename);

    /**
     * Inicializar el sistema de almacenamiento
     * (Crear carpetas locales o verificar conexión a Cloudinary)
     */
    void init();

    /**
     * Limpiar archivos antiguos (opcional)
     * @param daysOld Días de antigüedad
     */
    void cleanupOldFiles(int daysOld);
}