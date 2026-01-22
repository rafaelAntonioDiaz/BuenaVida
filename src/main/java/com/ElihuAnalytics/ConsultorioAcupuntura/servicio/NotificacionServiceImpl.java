package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// IMPORTS DE BREVO (Sendinblue)
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Servicio para enviar notificaciones nativas y correos electrónicos.
 * MIGRADO A BREVO.
 */
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // IMPORTANTE: Este correo debe ser el verificado en Brevo, o uno del mismo dominio
    private static final String MEDICO_EMAIL = "rafael.antonio.diaz@gmail.com";

    private final NotificacionNativaService nativaService;

    // Usamos la misma llave del properties, pero ahora contiene la de Brevo (xkeysib...)
    @Value("${sendgrid.api.key}")
    private String brevoApiKey;

    // Asegúrate de que este remitente esté verificado en Brevo
    @Value("${sendgrid.from.email:rafael.antonio.diaz@gmail.com}")
    private String fromEmail;

    public NotificacionServiceImpl(NotificacionNativaService nativaService) {
        this.nativaService = nativaService;
    }

    /**
     * Envía una confirmación al paciente (notificación nativa y correo).
     */
    @Override
    public void enviarConfirmacionPaciente(Sesion sesion, String mensaje) {
        logger.info("Enviando confirmación al paciente para sesión ID: {}", sesion.getId());
        try {
            nativaService.enviarNotificacionNativa(sesion , mensaje);
            String emailPaciente = sesion.getPaciente().getUsername();
            // Método privado actualizado a Brevo
            sendEmail(emailPaciente, "Confirmación de cita - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar confirmación al paciente para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    @Override
    public void enviarRecordatorioPaciente(Sesion sesion) {
        logger.info("Enviando recordatorio al paciente para sesión ID: {}", sesion.getId());
        try {
            String mensaje = "Recordatorio: Tienes una cita programada para el " +
                    sesion.getFecha().format(FORMATTER) +
                    ". Motivo: " + sesion.getMotivo() +
                    ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
            nativaService.enviarNotificacionNativa(sesion, mensaje);
            sendEmail(sesion.getPaciente().getUsername(), "Recordatorio de cita - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar recordatorio al paciente para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    @Override
    public void enviarRecordatorioMedico(Sesion sesion) {
        logger.info("Enviando recordatorio al médico para sesión ID: {}", sesion.getId());
        try {
            String mensaje = "Recordatorio: Cita con " +
                    sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                    " el " + sesion.getFecha().format(FORMATTER) +
                    ". Motivo: " + sesion.getMotivo() +
                    ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
            nativaService.enviarNotificacionNativa(sesion, mensaje);
            sendEmail(MEDICO_EMAIL, "Recordatorio de cita - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar recordatorio al médico para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    @Override
    public void enviarNotificacionProgramacionMedico(Sesion sesion) {
        logger.info("Enviando notificación de programación al médico para sesión ID: {}", sesion.getId());
        try {
            String mensaje = "Nueva cita programada por " +
                    sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                    " para el " + sesion.getFecha().format(FORMATTER) +
                    ". Motivo: " + sesion.getMotivo() +
                    ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
            nativaService.enviarNotificacionNativa(sesion, mensaje);
            sendEmail(MEDICO_EMAIL, "Nueva cita programada - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar notificación al médico para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    @Override
    public void enviarCancelacion(Sesion sesion) {
        logger.info("Enviando notificación de cancelacion para sesión ID: {}", sesion.getId());
        try {
            String mensaje = "Se cancela la cita de " +
                    sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                    " para el " + sesion.getFecha().format(FORMATTER) +
                    ". En: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");

            nativaService.enviarNotificacionNativa(sesion, mensaje);
            sendEmail(MEDICO_EMAIL, "Cita cancelada - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar notificación al médico para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    @Override
    public void enviarReprogramacionCita(Sesion sesion, LocalDateTime fechaAnterior) {
        logger.info("Enviando notificación de reprogramación para sesión del: {}", fechaAnterior.format(FORMATTER));
        try {
            String mensaje = "Se reprograma la cita de " +
                    sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                    " para el " + sesion.getFecha().format(FORMATTER) +
                    ". En: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");

            nativaService.enviarNotificacionNativa(sesion, mensaje);
            sendEmail(MEDICO_EMAIL, "Cita reprogramada - Acupuntura Buena Vida", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar notificación al médico para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    /**
     * Envía un correo usando la API de BREVO (Antes Sendinblue).
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param text Contenido del correo (Texto plano)
     */
    private void sendEmail(String to, String subject, String text) {
        // 1. Configurar cliente con la clave de Brevo
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        // 2. Configurar Remitente
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(fromEmail);
        sender.setName("Buena Vida Medicina");

        // 3. Configurar Destinatario
        SendSmtpEmailTo toEmail = new SendSmtpEmailTo();
        toEmail.setEmail(to);
        List<SendSmtpEmailTo> toList = Collections.singletonList(toEmail);

        // 4. Construir Correo
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(sender);
        sendSmtpEmail.setTo(toList);
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setTextContent(text); // Usamos TextContent porque tu lógica envía texto plano

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            logger.info("✅ Correo enviado a: {} vía Brevo", to);
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", to, e.getMessage());
        }
    }
}