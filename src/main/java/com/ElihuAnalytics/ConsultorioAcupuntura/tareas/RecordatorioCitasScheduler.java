package com.ElihuAnalytics.ConsultorioAcupuntura.tareas;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionNativaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecordatorioCitasScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioCitasScheduler.class);
    private final SesionService sesionService;
    private final NotificacionNativaService notificacionNativaService;

    public RecordatorioCitasScheduler(SesionService sesionService,
                                      NotificacionNativaService notificacionNativaService) {
        this.sesionService = sesionService;
        this.notificacionNativaService = notificacionNativaService;
    }

    // Ejecuta cada 15 minutos para enviar recordatorios de citas próximas
    @Scheduled(cron = "0 */15 * * * *")
    public void enviarRecordatorios() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime en2h = ahora.plusHours(2);

            List<Sesion> proximasSesiones = sesionService.buscarPendientesEntre(ahora, en2h);
            if (proximasSesiones.isEmpty()) {
                return; // No loggear si no hay sesiones
            }

            int recordatoriosEnviados = 0;
            for (Sesion sesion : proximasSesiones) {
                try {
                    // Envía notificación nativa para la sesión
                    notificacionNativaService.enviarNotificacionNativaCita(sesion);
                    recordatoriosEnviados++;
                } catch (Exception e) {
                    log.warn("Error enviando recordatorio para sesión {}: {}", sesion.getId(), e.getMessage());
                }
            }

            // Log solo si se enviaron recordatorios
            if (recordatoriosEnviados > 0) {
                log.info("Recordatorios enviados: {}/{}", recordatoriosEnviados, proximasSesiones.size());
            }

        } catch (Exception e) {
            log.warn("Error general en tarea de recordatorios: {}", e.getMessage());
        }
    }
}