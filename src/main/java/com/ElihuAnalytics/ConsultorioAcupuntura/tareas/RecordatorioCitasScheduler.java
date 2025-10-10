package com.ElihuAnalytics.ConsultorioAcupuntura.tareas;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler para enviar recordatorios automáticos 2 horas antes de las citas.
 */
@Component
public class RecordatorioCitasScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioCitasScheduler.class);
    private final SesionService sesionService;
    private final NotificacionService notificacionService;

    public RecordatorioCitasScheduler(SesionService sesionService, NotificacionService notificacionService) {
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
    }

    /**
     * Ejecuta cada 10 minutos para buscar citas confirmadas en las próximas 2 horas y enviar recordatorios.
     */
    @Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ventanaInicio = ahora.plusHours(2).minusMinutes(5);
        LocalDateTime ventanaFin = ahora.plusHours(2).plusMinutes(5);

        List<Sesion> proximasSesiones = sesionService.buscarPendientesEntre(ventanaInicio, ventanaFin);
        if (proximasSesiones.isEmpty()) {
            return;
        }

        int recordatoriosEnviados = 0;
        for (Sesion sesion : proximasSesiones) {
            if (sesion.getEstado() == Sesion.EstadoSesion.CONFIRMADA) {
                try {
                    notificacionService.enviarRecordatorioPaciente(sesion);
                    notificacionService.enviarRecordatorioMedico(sesion);
                    recordatoriosEnviados++;
                    log.info("Recordatorios enviados para sesión ID: {}", sesion.getId());
                } catch (Exception e) {
                    log.warn("Error al enviar recordatorios para sesión ID {}: {}", sesion.getId(), e.getMessage());
                }
            }
        }

        if (recordatoriosEnviados < proximasSesiones.size()) {
            log.warn("Solo se enviaron {} de {} recordatorios.", recordatoriosEnviados, proximasSesiones.size());
        }
    }
}