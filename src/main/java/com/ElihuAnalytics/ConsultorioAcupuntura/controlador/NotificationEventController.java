package com.ElihuAnalytics.ConsultorioAcupuntura.controlador;

import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationEventController {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventController.class);
    private final SesionService sesionService;

    public NotificationEventController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    // Maneja eventos de notificaciones (clicks, acciones)
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> handleNotificationEvent(@RequestBody Map<String, Object> payload) {
        try {
            String event = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            // Log solo en DEBUG para eventos recibidos
            log.debug("Evento de notificación recibido: {} con data: {}", event, data);

            switch (event) {
                case "notification_clicked":
                    handleNotificationClick(data);
                    break;
                case "action_confirmar":
                    handleConfirmarCita(data);
                    break;
                case "action_cancelar":
                    handleCancelarCita(data);
                    break;
                case "action_reagendar":
                    handleReagendarCita(data);
                    break;
                default:
                    log.warn("Evento no reconocido: {}", event);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Evento procesado"));

        } catch (Exception e) {
            log.warn("Error procesando evento de notificación: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error procesando evento"));
        }
    }

    private void handleNotificationClick(Map<String, Object> data) {
        Object sesionIdObj = data.get("sesionId");
        if (sesionIdObj != null) {
            Long sesionId = Long.valueOf(sesionIdObj.toString());
            log.debug("Usuario clickeó notificación para sesión: {}", sesionId);
        }
    }

    private void handleConfirmarCita(Map<String, Object> data) {
        Object sesionIdObj = data.get("sesionId");
        if (sesionIdObj != null) {
            Long sesionId = Long.valueOf(sesionIdObj.toString());
            log.debug("Confirmando cita desde notificación nativa: {}", sesionId);
            sesionService.confirmarSesion(sesionId).ifPresentOrElse(
                    sesion -> log.debug("Cita confirmada: {}", sesion.getId()),
                    () -> log.warn("No se pudo confirmar la cita: {}", sesionId)
            );
        }
    }

    private void handleCancelarCita(Map<String, Object> data) {
        Object sesionIdObj = data.get("sesionId");
        if (sesionIdObj != null) {
            Long sesionId = Long.valueOf(sesionIdObj.toString());
            log.debug("Cancelando cita desde notificación nativa: {}", sesionId);
            sesionService.cancelarSesion(sesionId).ifPresentOrElse(
                    sesion -> log.debug("Cita cancelada: {}", sesion.getId()),
                    () -> log.warn("No se pudo cancelar la cita: {}", sesionId)
            );
        }
    }

    private void handleReagendarCita(Map<String, Object> data) {
        Object sesionIdObj = data.get("sesionId");
        if (sesionIdObj != null) {
            Long sesionId = Long.valueOf(sesionIdObj.toString());
            log.debug("Solicitud de reagendamiento desde notificación nativa: {}", sesionId);
        }
    }
}