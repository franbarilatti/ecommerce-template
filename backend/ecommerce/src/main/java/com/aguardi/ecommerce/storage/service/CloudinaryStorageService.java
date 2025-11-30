// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/storage/service/CloudinaryStorageService.java
// Propósito: Implementación de almacenamiento con CLOUDINARY
// Uso: Producción
// ============================================

package com.aguardi.ecommerce.storage.service;

import com.aguardi.ecommerce.shared.dto.UploadResponse;
import com.aguardi.ecommerce.shared.exception.FileUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "cloudinary")
@Slf4j
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;
    private final String folderPrefix;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder:aguardi}") String folderPrefix
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        this.folderPrefix = folderPrefix;

        log.info("Cloudinary storage initialized with cloud: {}", cloudName);
        init();
    }

    @Override
    public void init() {
        try {
            // Verificar conexión con Cloudinary
            Map<?, ?> result = cloudinary.api().ping(ObjectUtils.emptyMap());
            log.info("Cloudinary connection verified: {}", result.get("status"));
        } catch (Exception e) {
            log.error("Could not connect to Cloudinary", e);
            throw new FileUploadException("No se pudo conectar con Cloudinary");
        }
    }

    @Override
    public UploadResponse uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("Uploading file to Cloudinary: {} in folder: {}", file.getOriginalFilename(), folder);

        // Validar archivo
        validateFile(file);

        try {
            // Generar public_id único
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String filenameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String publicId = String.format("%s/%s/%s_%s",
                    folderPrefix,
                    folder,
                    filenameWithoutExt,
                    UUID.randomUUID().toString().substring(0, 8)
            );

            // Subir a Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folderPrefix + "/" + folder,
                    "resource_type", "auto",
                    "overwrite", false,
                    "quality", "auto",
                    "fetch_format", "auto"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            String cloudinaryPublicId = (String) uploadResult.get("public_id");
            long size = ((Number) uploadResult.get("bytes")).longValue();
            String format = (String) uploadResult.get("format");

            log.info("File uploaded successfully to Cloudinary: {}", secureUrl);

            return UploadResponse.builder()
                    .url(secureUrl)
                    .publicId(cloudinaryPublicId)
                    .fileName(originalFilename)
                    .fileSize(size)
                    .message("Archivo subido a Cloudinary exitosamente")
                    .contentType(file.getContentType())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new FileUploadException("Error al subir archivo a Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public List<UploadResponse> uploadMultipleFiles(List<MultipartFile> files, String folder) throws IOException {
        log.info("Uploading {} files to Cloudinary in folder: {}", files.size(), folder);

        List<UploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                UploadResponse response = uploadFile(file, folder);
                responses.add(response);
            } catch (IOException e) {
                log.error("Error uploading file: {}", file.getOriginalFilename(), e);
                // Continuar con los demás archivos
            }
        }

        return responses;
    }

    @Override
    public boolean deleteFile(String publicId) {
        log.info("Deleting file from Cloudinary: {}", publicId);

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");

            boolean deleted = "ok".equals(resultStatus);

            if (deleted) {
                log.info("File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("File not found in Cloudinary: {}", publicId);
            }

            return deleted;

        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", publicId, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String publicId) {
        // En Cloudinary, el publicId ya contiene la URL completa normalmente
        // Si solo se pasa el public_id, construir la URL
        if (publicId.startsWith("http")) {
            return publicId;
        }

        return cloudinary.url().generate(publicId);
    }

    @Override
    public boolean fileExists(String publicId) {
        try {
            Map<?, ?> result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result != null && result.containsKey("public_id");
        } catch (Exception e) {
            log.debug("File does not exist in Cloudinary: {}", publicId);
            return false;
        }
    }

    @Override
    public void cleanupOldFiles(int daysOld) {
        log.info("Cleanup of old files in Cloudinary is not implemented");
        log.info("Use Cloudinary dashboard to manage old files or implement custom logic");

        // TODO: Implementar limpieza de archivos antiguos
        // Cloudinary tiene límites de API rate, así que esto debe hacerse con cuidado
        // Opción 1: Usar Cloudinary Admin API para listar y borrar recursos antiguos
        // Opción 2: Mantener un registro local de uploads y eliminar basado en fecha
    }

    // ========================================
    // MÉTODOS PRIVADOS
    // ========================================

    /**
     * Validar archivo antes de subirlo
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("El archivo está vacío");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Verificar caracteres inválidos
        if (originalFilename.contains("..")) {
            throw new FileUploadException("El nombre del archivo contiene una secuencia de ruta inválida");
        }

        // Verificar extensión permitida
        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".pdf");

        if (!allowedExtensions.contains(extension)) {
            throw new FileUploadException(
                    "Tipo de archivo no permitido. Solo se permiten: " + String.join(", ", allowedExtensions)
            );
        }

        // Verificar tamaño máximo (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB en bytes
        if (file.getSize() > maxSize) {
            throw new FileUploadException("El archivo excede el tamaño máximo permitido de 10MB");
        }
    }

    /**
     * Obtener extensión del archivo
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }
}