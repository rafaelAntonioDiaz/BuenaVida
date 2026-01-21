package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock
    private SendGrid sendGridMock; // Simulamos la librería SendGrid

    @Mock
    private Response responseMock; // Simulamos la respuesta de la API

    @Spy
    private CorreoService correoService; // Espiamos el servicio real

    @BeforeEach
    void setUp() {
        // Inyectamos valores falsos en las variables @Value privadas
        ReflectionTestUtils.setField(correoService, "sendGridApiKey", "SG.fake_key");
        ReflectionTestUtils.setField(correoService, "fromEmail", "test@consultorio.com");
    }

    @Test
    void debeEnviarCorreoCorrectamente_CuandoApiRespondeOK() throws IOException {
        // 1. PREPARACIÓN (GIVEN)
        String destino = "paciente@test.com";
        String codigo = "123456";

        // Simulamos que al pedir el cliente, devuelva nuestro MOCK
        doReturn(sendGridMock).when(correoService).crearClienteSendGrid();

        // Simulamos que la API responda "202 Accepted" (Éxito)
        when(responseMock.getStatusCode()).thenReturn(202);
        when(sendGridMock.api(any(Request.class))).thenReturn(responseMock);

        // 2. EJECUCIÓN (WHEN)
        correoService.enviarCodigo(destino, codigo);

        // 3. VERIFICACIÓN (THEN)
        // Capturamos el Request que se intentó enviar para revisarlo
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGridMock, times(1)).api(requestCaptor.capture());

        Request requestEnviado = requestCaptor.getValue();
        assertNotNull(requestEnviado);
        assertEquals("mail/send", requestEnviado.getEndpoint());

        // Verificamos que el cuerpo del mensaje contenga el correo destino y el código
        String body = requestEnviado.getBody();
        assertTrue(body.contains(destino), "El JSON debe contener el email destino");
        assertTrue(body.contains(codigo), "El JSON debe contener el código");
    }

    @Test
    void debeLanzarExcepcion_CuandoApiFalla() throws IOException {
        // 1. PREPARACIÓN
        doReturn(sendGridMock).when(correoService).crearClienteSendGrid();

        // Simulamos error 401 (Unauthorized)
        when(responseMock.getStatusCode()).thenReturn(401);
        when(sendGridMock.api(any(Request.class))).thenReturn(responseMock);

        // 2. y 3. EJECUCIÓN Y VERIFICACIÓN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            correoService.enviarCodigo("test@test.com", "000000");
        });

        assertTrue(exception.getMessage().contains("Error SendGrid"));
    }
}