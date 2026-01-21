package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Profile("!test")
public class CorreoService implements ICorreoService {

    private static final Logger logger = LoggerFactory.getLogger(CorreoService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Override
    public void enviarCodigo(String destinatario, String codigo) {
        Email from = new Email(fromEmail);
        String subject = "Código de verificación - Buena Vida";
        Email to = new Email(destinatario);
        Content content = new Content("text/plain", "Tu código es: " + codigo);
        Mail mail = new Mail(from, subject, to, content);

        // AQUÍ ESTÁ EL CAMBIO CLAVE: Usamos este método en lugar de 'new SendGrid()' directo
        SendGrid sg = crearClienteSendGrid();

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Correo enviado a {}. Status: {}", destinatario, response.getStatusCode());
            } else {
                logger.error("❌ Fallo SendGrid. Status: {}", response.getStatusCode());
                throw new RuntimeException("Error SendGrid: " + response.getStatusCode());
            }
        } catch (IOException ex) {
            logger.error("❌ Error conexión SendGrid: {}", ex.getMessage());
            throw new RuntimeException("Error de conexión correo", ex);
        }
    }

    // Método protegido para facilitar el Testing (Se puede 'espiar' y burlar)
    protected SendGrid crearClienteSendGrid() {
        return new SendGrid(sendGridApiKey);
    }
}