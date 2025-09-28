// Java
package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebStaticResourcesConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./pacientes-uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        String webPrefix = "/" + root.getFileName().toString() + "/**"; // -> "/pacientes-uploads/**"
        String location = root.toUri().toString();                      // -> "file:/.../pacientes-uploads/"

        registry.addResourceHandler(webPrefix)
                .addResourceLocations(location)
                .setCachePeriod(0);
    }
}