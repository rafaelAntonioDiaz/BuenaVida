package com.ElihuAnalytics.ConsultorioAcupuntura;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.profiles.active=test")
class AppTest {

    @Test
    void contextLoads() {
        // Prueba básica para verificar que el contexto de Spring se carga correctamente
        assertTrue(true);
    }
}