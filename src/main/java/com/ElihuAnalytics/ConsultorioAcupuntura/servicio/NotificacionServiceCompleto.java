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

    // Envía recordatorio al paciente, evitando duplicados
    @Override
    public void enviarRecordatorioPaciente(Sesion sesion) {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        if (notificacionRepository.existeNotificacionEnviada(
                sesion.getId(),
                NotificacionEnviada.TipoNotificacion.RECORDATORIO_PACIENTE,
                hace24h)) {
            return; // Evita duplicados
        }
        enviarNotificacionNativa(sesion, NotificacionEnviada.TipoNotificacion.RECORDATORIO_PACIENTE,
                "Recordatorio: Tienes una cita programada para el " +
                        sesion.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    // Envía confirmación al paciente
    @Override
    public void enviarConfirmacionPaciente(Sesion sesion, String mensaje) {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        if (notificacionRepository.existeNotificacionEnviada(
                sesion.getId(),
                NotificacionEnviada.TipoNotificacion.CONFIRMACION_PACIENTE,
                hace24h)) {
            return; // Evita duplicados
        }
        enviarNotificacionNativa(sesion, NotificacionEnviada.TipoNotificacion.CONFIRMACION_PACIENTE, mensaje);
    }

    // Envía notificación nativa y registra en la base de datos
    private void enviarNotificacionNativa(Sesion sesion, NotificacionEnviada.TipoNotificacion tipo, String mensaje) {
        // Envía la notificación nativa
        notificacionNativaService.enviarNotificacionNativaCita(sesion);
        // Registra en la base de datos
        NotificacionEnviada notificacion = new NotificacionEnviada(
                sesion.getId(),
                tipo,
                NotificacionEnviada.CanalNotificacion.PUSH,
                "native-notification"
        );
        notificacion.setEstado(NotificacionEnviada.EstadoNotificacion.ENVIADA);
        notificacionRepository.save(notificacion);
    }

    @Override
    public void enviarRecordatorioMedico(Sesion sesion) {
        // Implementar si es necesario
    }

    public void reenviarNotificacionesFallidas() {
        // Implementar si es necesario
    }
}