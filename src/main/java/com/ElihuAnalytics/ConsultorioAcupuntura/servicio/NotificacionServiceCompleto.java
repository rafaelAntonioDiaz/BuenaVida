package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotificacionEnviada;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.NotificacionEnviadaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class NotificacionServiceCompleto implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceCompleto.class);

    private final NotificacionEnviadaRepository notificacionRepository;
    private final NotificacionNativaService notificacionNativaService;

    public NotificacionServiceCompleto(NotificacionEnviadaRepository notificacionRepository,
                                       NotificacionNativaService notificacionNativaService) {
        this.notificacionRepository = notificacionRepository;
        this.notificacionNativaService = notificacionNativaService;
    }

    @Override
    public void enviarRecordatorioPaciente(Sesion sesion) {
        // Verificar duplicados
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);

        if (notificacionRepository.existeNotificacionEnviada(
                sesion.getId(),
                NotificacionEnviada.TipoNotificacion.RECORDATORIO_PACIENTE,
                hace24h)) {
            log.debug("Recordatorio ya enviado para sesión {} en las últimas 24h", sesion.getId());
            return;
        }

        boolean nativoEnviado = enviarNotificacionNativa(sesion);

        log.info("Recordatorios enviados para sesión {}: Nativo={}",
                sesion.getId(),  nativoEnviado);
    }

    private boolean enviarNotificacionNativa(Sesion sesion) {
        try {
            notificacionNativaService.enviarNotificacionNativaCita(sesion);

            // Registrar intento (las notificaciones nativas son locales, así que asumimos éxito)
            NotificacionEnviada notificacion = new NotificacionEnviada(
                    sesion.getId(),
                    NotificacionEnviada.TipoNotificacion.RECORDATORIO_PACIENTE,
                    NotificacionEnviada.CanalNotificacion.PUSH, // Usamos PUSH como categoría
                    "native-notification"
            );

            notificacion.setEstado(NotificacionEnviada.EstadoNotificacion.ENVIADA);
            notificacionRepository.save(notificacion);

            return true;

        } catch (Exception e) {
            log.error("Error enviando notificación nativa para sesión {}: {}", sesion.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public void enviarRecordatorioMedico(Sesion sesion) {
        // Implementación para recordatorio al médico

    }

    public void reenviarNotificacionesFallidas() {

    }
}
