// ============================================
// FILE: src/main/java/com/aguardi/ecommerce/storage/controller/StorageController.java
// Propósito: REST Controller para upload y manejo de archivos
// ============================================

package com.aguardi.ecommerce.storage.controller;

import com.aguardi.ecommerce.shared.dto.ApiResponse;
import com.aguardi.ecommerce.shared.dto.MessageResponse;
import com.aguardi.ecommerce.shared.dto.UploadResponse;
import com.aguardi.ecommerce.storage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Storage", description = "Endpoints para manejo de archivos e imágenes")
public class StorageController {

    private final StorageService storageService;

    // ========================================
    // ENDPOINTS DE UPLOAD (ADMIN)
    // ========================================

    /**
     * Subir un archivo
     * POST /api/storage/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "[ADMIN] Subir archivo",
            description = "Sube un archivo al sistema de almacenamiento (local o Cloudinary)"
    )
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFile(
            @Parameter(description = "Archivo a subir")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Carpeta destino (products, categories, etc)")
            @RequestParam(defaultValue = "general") String folder
    ) throws IOException {
        log.info("REST request to upload file: {} to folder: {}", file.getOriginalFilename(), folder);

        UploadResponse response = storageService.uploadFile(file, folder);

        return ResponseEntity.ok(ApiResponse.success(
                "Archivo subido exitosamente",
                response
        ));
    }

    /**
     * Subir múltiples archivos
     * POST /api/storage/upload-multiple
     */
    @PostMapping("/upload-multiple")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "[ADMIN] Subir múltiples archivos",
            description = "Sube varios archivos al mismo tiempo"
    )
    public ResponseEntity<ApiResponse<List<UploadResponse>>> uploadMultipleFiles(
            @Parameter(description = "Lista de archivos")
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "Carpeta destino")
            @RequestParam(defaultValue = "general") String folder
    ) throws IOException {
        log.info("REST request to upload {} files to folder: {}", files.size(), folder);

        List<UploadResponse> responses = storageService.uploadMultipleFiles(files, folder);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("%d archivos subidos exitosamente", responses.size()),
                responses
        ));
    }

    /**
     * Eliminar archivo
     * DELETE /api/storage/{publicId}
     */
    @DeleteMapping("/{folder}/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "[ADMIN] Eliminar archivo",
            description = "Elimina un archivo del sistema de almacenamiento"
    )
    public ResponseEntity<ApiResponse<Boolean>> deleteFile(
            @Parameter(description = "Carpeta del archivo")
            @PathVariable String folder,

            @Parameter(description = "Nombre del archivo")
            @PathVariable String filename
    ) {
        log.info("REST request to delete file: {}/{}", folder, filename);

        String publicId = folder + "/" + filename;
        boolean deleted = storageService.deleteFile(publicId);

        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Archivo eliminado exitosamente",
                    true
            ));
        } else {
            return ResponseEntity.ok(ApiResponse.success(
                    "Archivo no encontrado",
                    false
            ));
        }
    }

    // ========================================
    // ENDPOINTS PÚBLICOS (SERVIR ARCHIVOS LOCALES)
    // ========================================

    /**
     * Servir archivo estático (solo para storage local)
     * GET /api/storage/files/{folder}/{filename}
     *
     * Este endpoint solo funciona cuando storage.type=local
     * Con Cloudinary, los archivos se sirven directamente desde su CDN
     */
    @GetMapping("/files/{folder}/{filename:.+}")
    @Operation(
            summary = "Obtener archivo",
            description = "Descarga un archivo del almacenamiento local (solo cuando storage.type=local)"
    )
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Carpeta del archivo")
            @PathVariable String folder,

            @Parameter(description = "Nombre del archivo")
            @PathVariable String filename
    ) {
        try {
            log.debug("Serving file: {}/{}", folder, filename);

            // Construir ruta del archivo
            Path uploadDir = Paths.get("./uploads").toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(folder).resolve(filename).normalize();

            // Verificar que el archivo existe y está dentro del directorio permitido
            if (!filePath.startsWith(uploadDir)) {
                throw new SecurityException("Acceso denegado");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar content type
            String contentType;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException e) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving file: {}/{}", folder, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Verificar si un archivo existe
     * HEAD /api/storage/files/{folder}/{filename}
     */
    @RequestMapping(
            value = "/files/{folder}/{filename:.+}",
            method = RequestMethod.HEAD
    )
    @Operation(
            summary = "Verificar existencia de archivo",
            description = "Verifica si un archivo existe sin descargarlo"
    )
    public ResponseEntity<Void> checkFileExists(
            @PathVariable String folder,
            @PathVariable String filename
    ) {
        String publicId = folder + "/" + filename;
        boolean exists = storageService.fileExists(publicId);

        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Health check del módulo de storage
     * GET /api/storage/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verifica que el módulo de storage está funcionando"
    )
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(
                MessageResponse.success("Storage module is running")
        );
    }
}