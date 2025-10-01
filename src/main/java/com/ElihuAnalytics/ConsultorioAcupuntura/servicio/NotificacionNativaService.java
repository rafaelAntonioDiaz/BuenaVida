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

    // Envía notificación nativa solo si hay UI activa
    public void enviarNotificacionNativaCita(Sesion sesion) {
        Optional<UI> activeUI = getActiveUI();
        if (activeUI.isEmpty()) {
            return; // No loggear; es un caso esperado en tareas programadas
        }
        UI ui = activeUI.get();
        ui.access(() -> {
            String mensaje = "Cita programada para el " + sesion.getFecha().format(FORMATTER) +
                    " - Motivo: " + escaparTextoJavaScript(sesion.getMotivo());
            String script = buildNotificationScript(mensaje);
            ui.getPage().executeJs(script);
        });
    }

    // Construye el script JS para la notificación nativa
    private String buildNotificationScript(String mensaje) {
        String textoEscapado = escaparTextoJavaScript(mensaje);
        return String.format(
                "if (window.Notification && Notification.permission === 'granted') {" +
                        "  new Notification('%s');" +
                        "} else if (window.Notification && Notification.permission !== 'denied') {" +
                        "  Notification.requestPermission().then(p => { if(p === 'granted') { new Notification('%s'); } });" +
                        "}", textoEscapado, textoEscapado
        );
    }

    // Escapa texto para evitar inyecciones en JS
    private String escaparTextoJavaScript(String texto) {
        if (texto == null) return "";
        return texto.replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // Obtiene la UI activa sin generar logs
    private Optional<UI> getActiveUI() {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                return session.getUIs().stream().findFirst();
            }
        } catch (Exception ignored) {
            // No loggear; es normal en contextos sin UI
        }
        return Optional.empty();
    }
}