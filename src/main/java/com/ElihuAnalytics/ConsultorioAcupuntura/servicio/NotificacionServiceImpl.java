package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para enviar notificaciones nativas y correos electrónicos.
 */
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MEDICO_EMAIL = "rafael.antonio.diaz@gmail.com";
    private final NotificacionNativaService nativaService;

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email:clubwebapp2025@gmail.com}")
    private String fromEmail;

    public NotificacionServiceImpl(NotificacionNativaService nativaService) {
        this.nativaService = nativaService;
    }

    /**
     * Envía una confirmación al paciente (notificación nativa y correo).
     * @param sesion Sesión confirmada
     * @param mensaje Mensaje personalizado
     */
    @Override
    public void enviarConfirmacionPaciente(Sesion sesion, String mensaje) {
        logger.info("Enviando confirmación al paciente para sesión ID: {}", sesion.getId());
        try {

            nativaService.enviarNotificacionNativa(sesion , mensaje);
            String emailPaciente = sesion.getPaciente().getUsername();
            sendEmail(emailPaciente, "Confirmación de cita - Consultorio Acupuntura", mensaje);
        } catch (Exception e) {
            logger.error("Error al enviar confirmación al paciente para sesión ID {}: {}", sesion.getId(), e.getMessage());
        }
    }

    /**
     * Envía un recordatorio al paciente.
     * @param sesion Sesión para la que se envía el recordatorio
     */
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

    /**
     * Envía un recordatorio al médico.
     * @param sesion Sesión para la que se envía el recordatorio
     */
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

    /**
     * Envía una notificación al médico sobre una nueva cita programada.
     * @param sesion Sesión programada
     */
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
     * Envía un correo usando la API de SendGrid.
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param text Contenido del correo
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SendGrid sg = new SendGrid(sendgridApiKey);
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", text);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() == 202) {
                logger.info("✅ Correo enviado a: {}", to);
            } else {
                logger.error("❌ Error al enviar correo a {}: Status {}, Body: {}",
                        to, response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", to, e.getMessage(), e);
        }
    }
}