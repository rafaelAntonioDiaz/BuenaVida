// java
package com.ElihuAnalytics.ConsultorioAcupuntura.web;

import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> serve(@PathVariable String fileName) {
        Resource resource = (Resource) storage.loadByFileName(fileName);
        MediaType contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}