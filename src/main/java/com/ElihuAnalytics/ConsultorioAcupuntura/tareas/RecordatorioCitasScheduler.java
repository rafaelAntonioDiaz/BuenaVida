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
import java.util.stream.Collectors;

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

    // Ejecuta cada hora, limita a 50 sesiones
    @Scheduled(cron = "0 0 * * * *")
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en2h = ahora.plusHours(2);

        List<Sesion> proximasSesiones = sesionService.buscarPendientesEntre(ahora, en2h)
                .stream().limit(50).collect(Collectors.toList());
        if (proximasSesiones.isEmpty()) {
            return;
        }

        int recordatoriosEnviados = 0;
        for (Sesion sesion : proximasSesiones) {
            notificacionNativaService.enviarNotificacionNativaCita(sesion);
            recordatoriosEnviados++;
        }

        // Solo loguear si hay problemas (con WARN)
        if (recordatoriosEnviados > 0 && recordatoriosEnviados < proximasSesiones.size()) {
            log.warn("Solo se enviaron {} de {} recordatorios.", recordatoriosEnviados, proximasSesiones.size());
        }
    }
}