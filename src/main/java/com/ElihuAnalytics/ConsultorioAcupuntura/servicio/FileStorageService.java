package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation;

    public FileStorageService
            (@Value("${app.upload.dir:./pacientes-uploads/}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de subidas.", e);
        }
    }

    public String save(InputStream inputStream, String originalFileName) throws IOException {
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path destinationFile = this.rootLocation.resolve(uniqueFilename).normalize();

        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = Path.of(rootLocation.getFileName().toString(), uniqueFilename)
                .toString().replace("\\", "/");

        log.info("Archivo guardado exitosamente en: {}. Ruta relativa: {}", destinationFile, relativePath);
        return relativePath;
    }
    // Eliminar por ruta relativa devuelta por save(...)
    public boolean delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        try {
            // Tomar solo el nombre del archivo por seguridad
            Path fileName = Paths.get(relativePath).getFileName();
            if (fileName == null) return false;

            Path file = rootLocation.resolve(fileName).normalize();
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("Archivo eliminado: {}", file);
                return true;
            } else {
                log.warn("Archivo no encontrado para eliminar: {}", file);
                return false;
            }
        } catch (Exception e) {
            log.error("No se pudo eliminar el archivo '{}': {}", relativePath, e.getMessage(), e);
            return false;
        }
    }
    // Carga el archivo como Resource a partir del nombre de archivo (seguro dentro del root)
    public Resource loadByFileName(String fileName) {
        try {
            Path safeName = Paths.get(fileName).getFileName();
            if (safeName == null) {
                throw new IllegalArgumentException("Nombre de archivo inválido.");
            }
            Path file = rootLocation.resolve(safeName).normalize();
            UrlResource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Archivo no encontrado o no legible.");
            }
            return (Resource) resource;
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(new IOException("Ruta de archivo inválida.", e));
        }
    }
}