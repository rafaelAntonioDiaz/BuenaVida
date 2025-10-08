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

import java.time.format.DateTimeFormatter;

/**
 * Implementación de NotificacionService para enviar notificaciones nativas y correos.
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

    @Override
    public void enviarConfirmacionPaciente(Sesion sesion, String mensaje) {
        // Notificación nativa
        nativaService.enviarNotificacionNativaCita(sesion);

        // Correo al paciente
        String emailPaciente = sesion.getPaciente().getUsername();
        String subject = "Confirmación de cita - Consultorio Acupuntura";
        sendEmail(emailPaciente, subject, mensaje);
    }

    @Override
    public void enviarRecordatorioPaciente(Sesion sesion) {
        // Notificación nativa
        nativaService.enviarNotificacionNativaCita(sesion);

        // Correo al paciente
        String mensaje = "Recordatorio: Tienes una cita programada para el " +
                sesion.getFecha().format(FORMATTER) +
                ". Motivo: " + sesion.getMotivo() +
                ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
        sendEmail(sesion.getPaciente().getUsername(), "Recordatorio de cita - Consultorio Acupuntura", mensaje);
    }

    @Override
    public void enviarRecordatorioMedico(Sesion sesion) {
        // Notificación nativa
        nativaService.enviarNotificacionNativaCita(sesion);

        // Correo al médico
        String mensaje = "Recordatorio: Cita con " +
                sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                " el " + sesion.getFecha().format(FORMATTER) +
                ". Motivo: " + sesion.getMotivo() +
                ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
        sendEmail(MEDICO_EMAIL, "Recordatorio de cita - Consultorio Acupuntura", mensaje);
    }

    /**
     * Envía notificación nativa y correo al médico cuando se programa una cita.
     */
    public void enviarNotificacionProgramacionMedico(Sesion sesion) {
        // Notificación nativa
        nativaService.enviarNotificacionNativaCita(sesion);

        // Correo al médico
        String mensaje = "Nueva cita programada por " +
                sesion.getPaciente().getNombres() + " " + sesion.getPaciente().getApellidos() +
                " para el " + sesion.getFecha().format(FORMATTER) +
                ". Motivo: " + sesion.getMotivo() +
                ". Lugar: " + (sesion.getLugar() != null ? sesion.getLugar() : "Sin dirección");
        sendEmail(MEDICO_EMAIL, "Nueva cita programada - Consultorio Acupuntura", mensaje);
    }

    /**
     * Envía un correo usando SendGrid API.
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
                throw new RuntimeException("No se pudo enviar el correo");
            }
        } catch (Exception e) {
            logger.error("❌ Error al enviar correo a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }
}