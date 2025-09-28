package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test") // 👉 este solo se activa en test
@Primary
public class CorreoServiceStub implements ICorreoService {

    /**
     * Este servicio simula el envío de correos electrónicos en un entorno de prueba.
     * No envía correos reales, solo imprime mensajes en la consola.
     */
    @Override
    public void enviarCodigo(String destino, String codigo) {
        // No hace nada, solo imprime en consola
        System.out.println("[STUB] Correo simulado a " + destino + " con codigo: " + codigo);
    }
    @PostConstruct
    public void init() {
        System.out.println("✅ CorreoServiceStub cargado (perfil test)");
    }

}
