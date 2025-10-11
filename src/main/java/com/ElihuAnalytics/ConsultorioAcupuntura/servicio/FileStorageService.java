package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para gestionar el almacenamiento de archivos en el sistema de archivos.
 * Los archivos se guardan en el volumen de Railway configurado en app.upload.dir.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:/volumes/uploads/}")
    private String uploadDir;

    public FileStorageService() {
        // Constructor vacío para inyección de dependencias
    }

    /**
     * Guarda un archivo en el sistema de archivos y devuelve su ruta web relativa.
     *
     * @param inputStream Flujo de entrada del archivo a guardar
     * @param originalName Nombre original del archivo (se usa para extraer la extensión)
     * @return Ruta web relativa para acceder al archivo (ej: /pacientes-Uploads/abc-123.jpg)
     * @throws IOException si ocurre un error al crear directorios o guardar el archivo
     */
    public String save(InputStream inputStream, String originalName) throws IOException {
        try {
            // Asegurar que el directorio de subida existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Directorio de subida creado: {}", uploadPath.toAbsolutePath());
            }

            // Generar nombre único para el archivo
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Guardar el archivo
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Devolver ruta web relativa (debe coincidir con WebStaticResourcesConfig)
            String webPath = "/pacientes-Uploads/" + fileName;

            log.info("Archivo guardado exitosamente: originalName={}, fileName={}, filePath={}, webPath={}",
                    originalName, fileName, filePath.toAbsolutePath(), webPath);

            return webPath;
        } catch (IOException ex) {
            log.error("Error al guardar archivo: originalName={}, uploadDir={}, mensaje={}",
                    originalName, uploadDir, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Elimina un archivo del sistema de archivos.
     *
     * @param webPath Ruta web del archivo (ej: /pacientes-Uploads/abc-123.jpg)
     */
    public void delete(String webPath) {
        if (webPath == null || webPath.isBlank()) {
            log.warn("Intento de eliminar archivo con ruta nula o vacía");
            return;
        }

        try {
            // Extraer solo el nombre del archivo de la ruta web
            String fileName = webPath;

            // Si la ruta contiene /pacientes-Uploads/, extraer solo el nombre del archivo
            if (fileName.contains("/pacientes-Uploads/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/pacientes-Uploads/") + "/pacientes-Uploads/".length());
            } else if (fileName.contains("/uploads/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/uploads/") + "/uploads/".length());
            } else if (fileName.startsWith("/")) {
                // Si es una ruta que empieza con /, quitar el /
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }

            // Si contiene parámetros de query (?v=...), eliminarlos
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            Path filePath = Paths.get(uploadDir, fileName);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Archivo eliminado exitosamente: webPath={}, filePath={}", webPath, filePath.toAbsolutePath());
            } else {
                log.warn("Archivo no encontrado para eliminar: webPath={}, filePath={}", webPath, filePath.toAbsolutePath());
            }
        } catch (IOException ex) {
            log.error("Error al eliminar archivo: webPath={}, mensaje={}", webPath, ex.getMessage(), ex);
        }
    }

    /**
     * Verifica si un archivo existe en el sistema de archivos.
     *
     * @param webPath Ruta web del archivo
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean exists(String webPath) {
        if (webPath == null || webPath.isBlank()) {
            return false;
        }

        try {
            String fileName = webPath;
            if (fileName.contains("/pacientes-Uploads/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/pacientes-Uploads/") + "/pacientes-Uploads/".length());
            }
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            Path filePath = Paths.get(uploadDir, fileName);
            return Files.exists(filePath);
        } catch (Exception ex) {
            log.error("Error al verificar existencia del archivo: webPath={}, mensaje={}", webPath, ex.getMessage());
            return false;
        }
    }
}