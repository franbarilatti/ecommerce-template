// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/storage/service/LocalStorageService.java
// Propósito: Implementación de almacenamiento LOCAL en filesystem
// Uso: Desarrollo y testing
// ============================================

package com.aguardi.ecommerce.storage.service;

import com.aguardi.ecommerce.shared.dto.UploadResponse;
import com.aguardi.ecommerce.shared.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public LocalStorageService(
            @Value("${storage.local.upload-dir:./uploads}") String uploadDir,
            @Value("${storage.local.base-url:http://localhost:8080/api/storage/files}") String baseUrl
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;

        log.info("Local storage initialized at: {}", this.uploadDir);
        init();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Upload directory created: {}", uploadDir);
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new FileUploadException("No se pudo crear el directorio de uploads");
        }
    }

    @Override
    public UploadResponse uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("Uploading file: {} to folder: {}", file.getOriginalFilename(), folder);

        // Validar archivo
        validateFile(file);

        // Crear carpeta si no existe
        Path folderPath = uploadDir.resolve(folder);
        Files.createDirectories(folderPath);

        // Generar nombre único
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path targetLocation = folderPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Construir URL
        String fileUrl = String.format("%s/%s/%s", baseUrl, folder, uniqueFilename);

        log.info("File uploaded successfully: {}", fileUrl);

        return UploadResponse.builder()
                .url(fileUrl)
                .publicId(folder + "/" + uniqueFilename)
                .fileName(uniqueFilename)
                .fileSize(file.getSize())
                .message("Archivo subido exitosamente")
                .contentType(file.getContentType())
                .build();
    }

    @Override
    public List<UploadResponse> uploadMultipleFiles(List<MultipartFile> files, String folder) throws IOException {
        log.info("Uploading {} files to folder: {}", files.size(), folder);

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
        log.info("Deleting file: {}", publicId);

        try {
            Path filePath = uploadDir.resolve(publicId);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", publicId);
                return true;
            } else {
                log.warn("File not found: {}", publicId);
                return false;
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", publicId, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String filename) {
        return String.format("%s/%s", baseUrl, filename);
    }

    @Override
    public boolean fileExists(String filename) {
        Path filePath = uploadDir.resolve(filename);
        return Files.exists(filePath);
    }

    @Override
    public void cleanupOldFiles(int daysOld) {
        log.info("Cleaning up files older than {} days", daysOld);

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minus(daysOld, ChronoUnit.DAYS);

            Files.walk(uploadDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            LocalDateTime fileTime = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(path).toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );
                            return fileTime.isBefore(cutoffDate);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("Deleted old file: {}", path);
                        } catch (IOException e) {
                            log.error("Error deleting old file: {}", path, e);
                        }
                    });

            log.info("Cleanup completed");
        } catch (IOException e) {
            log.error("Error during cleanup", e);
        }
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
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg");

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