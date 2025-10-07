package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Profile("prod")  // Este bean se carga solo en perfil 'prod'
public class SendGridCorreoService implements ICorreoService {

    private static final Logger logger = LoggerFactory.getLogger(SendGridCorreoService.class);

    @Value("${sendgrid.api.key}")  // API key de SendGrid desde variables de entorno
    private String sendgridApiKey;

    @Value("${sendgrid.from.email}")  // Email remitente verificado (ej. clubwebapp2025@gmail.com)
    private String fromEmail;

    @Override
    public void enviarCodigo(String destino, String codigo) {
        try {
            // Configura el cliente SendGrid con la API key
            SendGrid sg = new SendGrid(sendgridApiKey);

            // Crea el correo
            Email from = new Email(fromEmail);
            Email to = new Email(destino);
            Content content = new Content("text/plain", "Tu código de verificación es: " + codigo + "\n\nEste código es válido por 10 minutos.");
            Mail mail = new Mail(from, "Código de verificación - Consultorio Acupuntura", to, content);

            // Envía el correo via API HTTP
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            // Verifica respuesta
            if (response.getStatusCode() == 202) {
                logger.info("✅ Código enviado a: {}", destino);
            } else {
                logger.error("❌ Error al enviar correo a {}: Status {}, {}", destino, response.getStatusCode(), response.getBody());
                throw new RuntimeException("No se pudo enviar el correo de verificación");
            }
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", destino, e.getMessage(), e);
            throw new RuntimeException("No se pudo conectar al servidor de correo. Por favor, verifica la configuración.", e);
        }
    }
}