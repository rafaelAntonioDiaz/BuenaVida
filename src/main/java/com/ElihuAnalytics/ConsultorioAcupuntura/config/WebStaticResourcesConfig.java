package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebStaticResourcesConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapear /pacientes-Uploads/ para archivos de pacientes (Railway Volumes o local)
        registry.addResourceHandler("/pacientes-Uploads/**")
                .addResourceLocations("file:" + uploadDir);

        // Mapear /images/ para imágenes estáticas del frontend
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/META-INF/resources/images/");
    }
}