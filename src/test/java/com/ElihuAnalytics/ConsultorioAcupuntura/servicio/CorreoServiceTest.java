package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock
    private JavaMailSender mailSender; // Simulamos el enviador real

    @InjectMocks
    private CorreoService correoService; // El servicio que estamos probando

    @Test
    void debeEnviarCorreoCorrectamente() {
        // DATOS DE PRUEBA
        String destino = "paciente@test.com";
        String codigo = "123456";

        // EJECUCIÓN
        correoService.enviarCodigo(destino, codigo);

        // VERIFICACIÓN (Capturamos qué le pasó el servicio al mailSender)
        ArgumentCaptor<SimpleMailMessage> mensajeCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(mensajeCaptor.capture());

        SimpleMailMessage mensajeEnviado = mensajeCaptor.getValue();

        // ASERCIONES
        assertEquals(destino, mensajeEnviado.getTo()[0]);
        assertEquals("Código de verificación - Consultorio Acupuntura", mensajeEnviado.getSubject());
        assertTrue(mensajeEnviado.getText().contains(codigo));
    }
}