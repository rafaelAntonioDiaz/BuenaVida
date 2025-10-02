package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Profile("!test") // Este bean NO se carga cuando el perfil activo es "test"
public class CorreoService implements ICorreoService {

    private static final Logger logger = LoggerFactory.getLogger(CorreoService.class);
    private final JavaMailSender mailSender;

    public CorreoService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCodigo(String destino, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destino);
            mensaje.setSubject("Código de verificación - Consultorio Acupuntura");
            mensaje.setText("Tu código de verificación es: " + codigo + "\n\nEste código es válido por 10 minutos.");
            mailSender.send(mensaje);
            logger.info("✅ Código enviado a: {}", destino);
        } catch (MailException e) {
            logger.error("❌ Error al enviar correo a {}: {}", destino, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de verificación", e);
        }
    }
}