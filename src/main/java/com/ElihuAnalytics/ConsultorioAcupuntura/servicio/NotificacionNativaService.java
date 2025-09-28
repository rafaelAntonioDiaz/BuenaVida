package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class NotificacionNativaService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionNativaService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviarNotificacionNativaCita(Sesion sesion) {
        try {
            // Verificar si hay una sesión UI activa
            Optional<UI> activeUI = getActiveUI();

            if (activeUI.isPresent()) {
                UI ui = activeUI.get();

                ui.access(() -> {
                    String script = buildNotificationScript(sesion);
                    ui.getPage().executeJs(script);
                });

                log.info("Notificación nativa enviada para sesión: {}", sesion.getId());
            } else {
                log.debug("No hay UI activa para enviar notificación nativa, sesión: {}", sesion.getId());
            }

        } catch (Exception e) {
            log.error("Error enviando notificación nativa para sesión {}: {}", sesion.getId(), e.getMessage());
        }
    }

    private String buildNotificationScript(Sesion sesion) {
        String fecha = sesion.getFecha().format(FORMATTER);
        String paciente = escaparTextoJavaScript(sesion.getPaciente().getNombres());
        String motivo = escaparTextoJavaScript(sesion.getMotivo());
        String lugar = sesion.getLugar() != null ? escaparTextoJavaScript(sesion.getLugar()) : "";

        return String.format(
                "if (window.notificationService) {" +
                        "  window.notificationService.showAppointmentConfirmation(%d, '%s', '%s', '%s');" +
                        "} else {" +
                        "  console.warn('NotificationService no está disponible');" +
                        "}",
                sesion.getId(), fecha, paciente, motivo
        );
    }

    private String escaparTextoJavaScript(String texto) {
        if (texto == null) return "";
        return texto.replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private Optional<UI> getActiveUI() {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                return session.getUIs().stream().findFirst();
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener UI activa: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void enviarNotificacionProgramada(Sesion sesion, long delayMillis) {
        Optional<UI> activeUI = getActiveUI();

        if (activeUI.isPresent()) {
            UI ui = activeUI.get();

            ui.access(() -> {
                String script = String.format(
                        "if (window.notificationService) {" +
                                "  const scheduledTime = Date.now() + %d;" +
                                "  window.notificationService.scheduleNotification(" +
                                "    '🏥 Recordatorio de Cita'," +
                                "    {" +
                                "      body: 'Tu cita de acupuntura es en 2 horas'," +
                                "      icon: '/icons/icon-192x192.png'," +
                                "      requireInteraction: true," +
                                "      data: { sesionId: %d }" +
                                "    }," +
                                "    scheduledTime" +
                                "  );" +
                                "}",
                        delayMillis, sesion.getId()
                );

                ui.getPage().executeJs(script);
            });

            log.info("Notificación programada para sesión {} en {} ms", sesion.getId(), delayMillis);
        }
    }
}
