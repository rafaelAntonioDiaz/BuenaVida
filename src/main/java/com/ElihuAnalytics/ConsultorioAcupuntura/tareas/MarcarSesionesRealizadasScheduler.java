package com.ElihuAnalytics.ConsultorioAcupuntura.tareas;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler para marcar sesiones como REALIZADA cuando su fecha y hora han pasado.
 */
@Component
public class MarcarSesionesRealizadasScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarcarSesionesRealizadasScheduler.class);
    private final SesionService sesionService;

    public MarcarSesionesRealizadasScheduler(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void marcarSesionesRealizadas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Sesion> sesionesPendientes = sesionService.buscarPendientesAntes(ahora);

        if (sesionesPendientes.isEmpty()) {
            return;
        }

        int sesionesMarcadas = 0;
        for (Sesion sesion : sesionesPendientes) {
            if (sesion.getEstado() == Sesion.EstadoSesion.PROGRAMADA || sesion.getEstado() == Sesion.EstadoSesion.CONFIRMADA) {
                try {
                    sesion.setEstado(Sesion.EstadoSesion.REALIZADA);
                    sesionService.guardarSesion(sesion);
                    sesionesMarcadas++;
                    log.info("Sesión ID {} marcada como REALIZADA", sesion.getId());
                } catch (Exception e) {
                    log.warn("Error al marcar sesión ID {} como REALIZADA: {}", sesion.getId(), e.getMessage());
                }
            }
        }

        if (sesionesMarcadas < sesionesPendientes.size()) {
            log.warn("Solo se marcaron {} de {} sesiones como REALIZADA.", sesionesMarcadas, sesionesPendientes.size());
        }
    }
}