package com.ElihuAnalytics.ConsultorioAcupuntura;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 👉 fuerza a que use solo los beans con @Profile("test")
class ConsultorioDeAcupunturaApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto levanta con éxito, este test pasa.
    }
}
