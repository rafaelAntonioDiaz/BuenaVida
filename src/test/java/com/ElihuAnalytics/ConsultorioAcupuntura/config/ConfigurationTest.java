package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.ICorreoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        // Inyectamos valores falsos para que Spring no busque la variable de entorno real
        "sendgrid.api.key=SG.clave_falsa_para_testing",
        "sendgrid.from.email=test@dominio.com"
})
@ActiveProfiles("dev")
public class ConfigurationTest {

    @Autowired
    private ICorreoService correoService;

    @Test
    void elContextoDebeCargarElServicioDeCorreo() {
        // Esta prueba verifica que la fábrica de beans de Spring logró
        // construir todo el sistema de correo sin explotar.
        assertNotNull(correoService, "El servicio de correo debería estar disponible en el contexto");

        System.out.println("✅ PRUEBA DE INTEGRACIÓN EXITOSA: El contexto levantó correctamente.");
    }
}