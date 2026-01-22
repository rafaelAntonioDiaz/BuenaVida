package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CorreoService implements ICorreoService {

    private static final Logger logger = LoggerFactory.getLogger(CorreoService.class);

    @Value("${sendgrid.api.key}") // Usaremos la misma variable en properties para no cambiar todo, pero pon la clave de Brevo ahí
    private String brevoApiKey;

    @Value("${sendgrid.from.email}") // Tu remitente verificado en Brevo
    private String fromEmail;

    @Override
    public void enviarCodigo(String destinatario, String codigo) {
        // 1. Configurar cliente Brevo
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        // 2. Configurar el correo
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(fromEmail);
        sender.setName("Buena Vida Medicina");

        SendSmtpEmailTo to = new SendSmtpEmailTo();
        to.setEmail(destinatario);
        List<SendSmtpEmailTo> toList = Collections.singletonList(to);

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(sender);
        sendSmtpEmail.setTo(toList);
        sendSmtpEmail.setSubject("Código de verificación - Buena Vida");
        sendSmtpEmail.setTextContent("Hola,\n\nTu código de verificación es: " + codigo +
                "\n\nÚsalo para completar tu registro.");

        // 3. Enviar
        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            logger.info("✅ Correo enviado a {} vía Brevo.", destinatario);
        } catch (Exception e) {
            logger.error("❌ Error enviando correo con Brevo: {}", e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de verificación");
        }
    }
}