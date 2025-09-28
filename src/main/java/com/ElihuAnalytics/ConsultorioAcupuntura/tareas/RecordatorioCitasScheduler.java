package com.ElihuAnalytics.ConsultorioAcupuntura.tareas;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionNativaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.util.Broadcaster;
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

    // Ejecutar cada 15 minutos para recordatorios
    @Scheduled(cron = "0 */15 * * * *")
    public void enviarRecordatorios() {
        log.debug("Iniciando proceso de recordatorios...");

        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime en2h = ahora.plusHours(2);

            List<Sesion> proximasSesiones = sesionService.buscarPendientesEntre(ahora, en2h);

            log.info("Encontradas {} sesiones próximas para recordatorio", proximasSesiones.size());

            int recordatoriosEnviados = 0;
            for (Sesion sesion : proximasSesiones) {
                Broadcaster.broadcast("Recordatorio: tienes una cita a las "
                        + sesion.getFecha().toLocalTime());
                try {
                    // 🚀 Solo notificación nativa
                    notificacionNativaService.enviarNotificacionNativaCita(sesion);
                    recordatoriosEnviados++;
                } catch (Exception e) {
                    log.error("Error enviando recordatorio para sesión {}: {}", sesion.getId(), e.getMessage());
                }
            }

            log.info("Proceso de recordatorios completado: {}/{} sesiones procesadas",
                    recordatoriosEnviados, proximasSesiones.size());

        } catch (Exception e) {
            log.error("Error en proceso de recordatorios: {}", e.getMessage(), e);
        }
    }

    // Puedes eliminar este si ya no manejas reintentos por correo
    @Scheduled(cron = "0 0 * * * *")
    public void reenviarNotificacionesFallidas() {
        log.debug("Reenvío de notificaciones fallidas deshabilitado porque ahora solo usamos nativas.");
    }
}
