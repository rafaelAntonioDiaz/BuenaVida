package com.ElihuAnalytics.ConsultorioAcupuntura.tareas;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotificacionEnviada; // <-- Importar
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.EstadoSesion; // <-- Importar EstadoSesion
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.NotificacionEnviadaRepository; // <-- Importar Repositorio
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // <-- Importar Transactional

import java.time.LocalDateTime;
import java.time.LocalTime; // <-- Importar LocalTime
import java.util.List;

/**
 * Scheduler para enviar recordatorios automáticos de citas confirmadas.
 * Reglas: 2 horas antes, o día anterior a las 7 PM si la cita es antes de las 9 AM.
 */
@Component
public class RecordatorioCitasScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioCitasScheduler.class);
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final NotificacionEnviadaRepository notificacionEnviadaRepository; // <-- Inyectar Repositorio

    // --- Constantes para la Lógica ---
    private static final LocalTime HORA_LIMITE_MANANA = LocalTime.of(9, 0); // 9 AM
    private static final LocalTime HORA_ENVIO_NOCTURNO = LocalTime.of(19, 0); // 7 PM
    // Asegúrate que TipoNotificacion.RECORDATORIO_CITA exista en tu Enum
    private static final NotificacionEnviada.TipoNotificacion TIPO_NOTIFICACION = NotificacionEnviada.TipoNotificacion.CONFIRMACION_CITA;
    private static final int FRECUENCIA_SCHEDULER_MINUTOS = 15; // Ejecutar cada 15 min
    public RecordatorioCitasScheduler(SesionService sesionService,
                                      NotificacionService notificacionService,
                                      NotificacionEnviadaRepository notificacionEnviadaRepository) { // <-- Añadir al constructor
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.notificacionEnviadaRepository = notificacionEnviadaRepository; // <-- Asignar
    }

    /**
     * Se ejecuta periódicamente para buscar citas confirmadas que necesiten recordatorio.
     */
    @Scheduled(fixedRate = FRECUENCIA_SCHEDULER_MINUTOS * 60000) // Convertir minutos a ms
    @Transactional // Para leer y escribir en NotificacionEnviadaRepository
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();
        log.debug("Ejecutando scheduler de recordatorios a las {}", ahora);

        // Ventana de búsqueda: desde ahora hasta suficiente tiempo adelante para cubrir ambos casos
        LocalDateTime ventanaInicio = ahora;
        // Buscamos hasta 25 horas adelante para incluir citas de mañana < 9 AM
        LocalDateTime ventanaFin = ahora.plusHours(25);

        // Usamos el método específico para buscar confirmadas
        List<Sesion> proximasSesionesConfirmadas = sesionService.findSesionesConfirmadasEntre(ventanaInicio, ventanaFin);
        log.debug("Se encontraron {} sesiones confirmadas en la ventana {} - {}",
                proximasSesionesConfirmadas.size(), ventanaInicio, ventanaFin);

        if (proximasSesionesConfirmadas.isEmpty()) {
            return;
        }

        int recordatoriosEnviados = 0;
        for (Sesion sesion : proximasSesionesConfirmadas) {
            // Verificar si ya se envió un recordatorio ANTES de calcular tiempos
            boolean yaEnviado = verificarSiYaSeEnvio(sesion.getId());
            if (yaEnviado) {
                log.trace("Recordatorio para sesión ID {} ya fue enviado.", sesion.getId());
                continue;
            }


            LocalDateTime fechaCita = sesion.getFecha();
            LocalTime horaCita = fechaCita.toLocalTime();
            boolean enviarAhora = false;

            // --- Lógica de Tiempo ---
            if (horaCita.isBefore(HORA_LIMITE_MANANA)) {
                // Cita < 9 AM: Enviar día anterior ~7 PM
                LocalDateTime momentoEnvioNocturno = fechaCita.toLocalDate().minusDays(1).atTime(HORA_ENVIO_NOCTURNO);
                // Comprobar si 'ahora' está dentro de la ventana de ejecución del scheduler alrededor de las 7 PM
                if (ahora.isAfter(momentoEnvioNocturno.minusMinutes(FRECUENCIA_SCHEDULER_MINUTOS / 2)) &&
                        ahora.isBefore(momentoEnvioNocturno.plusMinutes(FRECUENCIA_SCHEDULER_MINUTOS / 2 + 1))) { // Ventana basada en frecuencia
                    enviarAhora = true;
                    log.debug("Sesión ID {} (< 9 AM). Cumple ventana de envío nocturno.", sesion.getId());
                }
            } else {
                // Cita >= 9 AM: Enviar 2 horas antes
                LocalDateTime momentoEnvioNormal = fechaCita.minusHours(2);
                // Comprobar si 'ahora' está dentro de la ventana de ejecución alrededor de 2h antes
                if (ahora.isAfter(momentoEnvioNormal.minusMinutes(FRECUENCIA_SCHEDULER_MINUTOS / 2)) &&
                        ahora.isBefore(momentoEnvioNormal.plusMinutes(FRECUENCIA_SCHEDULER_MINUTOS / 2 + 1))) { // Ventana basada en frecuencia
                    enviarAhora = true;
                    log.debug("Sesión ID {} (>= 9 AM). Cumple ventana de envío 2h antes.", sesion.getId());
                }
            }

            // --- Envío y Registro ---
            if (enviarAhora) {
                try {
                    log.info("Enviando recordatorios (paciente y médico) para sesión ID: {}", sesion.getId());
                    // Asegúrate que estos métodos existan en NotificacionService
                    notificacionService.enviarRecordatorioPaciente(sesion);
                    notificacionService.enviarRecordatorioMedico(sesion);

                    // Registrar envío
                    registrarEnvio(sesion, ahora);

                    recordatoriosEnviados++;
                    log.info("Recordatorios enviados y registrados para sesión ID: {}", sesion.getId());

                } catch (Exception e) {
                    log.error("Error al enviar o registrar recordatorios para sesión ID {}: {}", sesion.getId(), e.getMessage(), e);
                    // Considera registrar el fallo en NotificacionEnviada con estado FALLIDA
                }
            } else {
                log.trace("Aún no es momento para enviar recordatorio para sesión ID {}", sesion.getId());
            }
        } // Fin del for

        log.debug("Scheduler finalizado. {} recordatorios enviados.", recordatoriosEnviados);
    }

    /**
     * Verifica en la base de datos si ya se envió un recordatorio para la sesión dada.
     */
    private boolean verificarSiYaSeEnvio(Long sesionId) {
        // Usa el método simple del repositorio que asume TipoNotificacion
        List<NotificacionEnviada> enviadas = notificacionEnviadaRepository.findBySesionIdAndTipo(sesionId, TIPO_NOTIFICACION);
        // Considera solo las enviadas con éxito si tienes un campo 'estado' en NotificacionEnviada
        // return enviadas.stream().anyMatch(n -> "ENVIADA".equals(n.getEstado()));
        return !enviadas.isEmpty(); // Devuelve true si encuentra alguna (simple)
    }

    /**
     * Guarda un registro en la tabla NotificacionEnviada.
     */
    private void registrarEnvio(Sesion sesion, LocalDateTime fechaEnvio) {
        NotificacionEnviada registro = new NotificacionEnviada();
        registro.setSesionId(sesion.getId());
        // O si solo guardas el ID: registro.setSesionId(sesion.getId());
        registro.setTipo(TIPO_NOTIFICACION);
        registro.setFechaEnvio(fechaEnvio);
        // Si tienes estado: registro.setEstado("ENVIADA");
        // Si tienes intentos: registro.setIntentos(1);
        notificacionEnviadaRepository.save(registro);
    }
}