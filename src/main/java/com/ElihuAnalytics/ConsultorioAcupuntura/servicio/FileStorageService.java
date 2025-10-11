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
 * Servicio para gestionar el almacenamiento de archivos.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:/volumes/uploads/}")
    private String uploadDir;

    @Value("${app.base-url:https://your-railway-app.up.railway.app}")
    private String baseUrl;

    public FileStorageService() {
        // Constructor vacío para inyección de dependencias
    }

    /**
     * Guarda un archivo y devuelve su ruta relativa.
     * @param inputStream Flujo de entrada del archivo
     * @param originalName Nombre original del archivo
     * @return Ruta relativa del archivo guardado
     * @throws IOException si ocurre un error al guardar
     */
    public String save(InputStream inputStream, String originalName) throws IOException {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Directorio de subida creado: {}", uploadPath);
            }

            String extension = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            String relativePath = "/uploads/" + fileName;
            String fullUrl = baseUrl + relativePath;
            log.info("Archivo guardado: originalName={}, path={}, fullUrl={}", originalName, filePath, fullUrl);
            return fullUrl; // Devolver URL absoluta
        } catch (IOException ex) {
            log.error("Error al guardar archivo: originalName={}, mensaje={}", originalName, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Elimina un archivo dado su ruta relativa.
     * @param relativePath Ruta relativa del archivo
     */
    public void delete(String relativePath) {
        try {
            String fileName = relativePath.replace(baseUrl + "/uploads/", "");
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado: path={}", filePath);
        } catch (IOException ex) {
            log.error("Error al eliminar archivo: relativePath={}, mensaje={}", relativePath, ex.getMessage(), ex);
        }
    }
}