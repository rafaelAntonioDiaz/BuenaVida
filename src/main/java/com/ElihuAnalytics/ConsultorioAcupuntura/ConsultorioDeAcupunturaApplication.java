package com.ElihuAnalytics.ConsultorioAcupuntura;

import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Clase principal.
 * Forzamos el escaneo de:
 * - Entidades JPA (modelo)
 * - Repositorios Spring Data
 * - Componentes/servicios
 *
 * Esto elimina el problema clásico de que Hibernate no cree tablas porque no “ve” las entidades.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ElihuAnalytics.ConsultorioAcupuntura")
@EntityScan(basePackages = "com.ElihuAnalytics.ConsultorioAcupuntura.modelo")
@EnableJpaRepositories(basePackages = "com.ElihuAnalytics.ConsultorioAcupuntura.repositorio")
public class ConsultorioDeAcupunturaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultorioDeAcupunturaApplication.class, args);
    }
    // Pequeña verificación al arrancar: si esto se imprime, tu contexto cargó bien.
    @Bean
    public org.springframework.boot.CommandLineRunner startupLog() {
        return args -> System.out.println("Contexto levantado: escaneo de entidades/repos habilitado.");
    }

}