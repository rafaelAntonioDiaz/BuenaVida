package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.EstadoSesion; // Necesario
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository; // Asegúrate que este existe y es necesario
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.SesionRepository;
// Importa tu clase de festivos
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.FestivosColombia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Necesario para el constructor
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*; // Importa todo java.time
import java.util.ArrayList; // Necesario
import java.util.Arrays; // Necesario para EnumSet o List
import java.util.EnumSet; // Necesario para filtrar estados
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Necesario si usas streams

import static com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion.EstadoSesion.*;
import java.time.format.DateTimeFormatter;


@Service
public class SesionServiceImpl implements SesionService {

    private static final Logger log = LoggerFactory.getLogger(SesionServiceImpl.class);

    // --- Constantes para Agendamiento (Ajusta según tu necesidad) ---
    private static final LocalTime HORA_INICIO_LABORAL = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_LABORAL_NORMAL = LocalTime.of(17, 0); // 5 PM
    private static final LocalTime HORA_FIN_LABORAL_RESTRINGIDA = LocalTime.of(14, 0); // 2 PM para L, M, V
    private static final Duration DURACION_SESION_PREDET = Duration.ofHours(1); // Duración estándar
    private static final Duration TIEMPO_DESPLAZAMIENTO = Duration.ofMinutes(30);
    private static final Duration INTERVALO_AGENDAMIENTO = Duration.ofMinutes(30); // Pasos para buscar disponibilidad

    private final SesionRepository sesionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    // Inyecta PacienteRepository si lo usas
    // private final PacienteRepository pacienteRepository;
    // Inyecta NotificacionService (Asegúrate que exista la interfaz/clase)
    private final NotificacionService notificacionService;

    // --- Constructor (Asegúrate que coincida con tus Beans) ---
    @Autowired
    public SesionServiceImpl(SesionRepository sesionRepository, NotificacionService notificacionService /*, PacienteRepository pacienteRepository*/) {
        this.sesionRepository = sesionRepository;
        this.notificacionService = notificacionService;
        // this.pacienteRepository = pacienteRepository;
    }

    // --- IMPLEMENTACIÓN getHorasDisponibles ---
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> getHorasDisponibles(LocalDate fecha) {
        List<LocalTime> horariosDisponibles = new ArrayList<>();
        log.debug("Calculando horas disponibles para fecha: {}", fecha);

        // 1. Validaciones iniciales
        if (fecha.isBefore(LocalDate.now())) {
            log.debug("Fecha {} es pasada.", fecha); return horariosDisponibles;
        }
        if (FestivosColombia.esFestivo(fecha)) {
            log.debug("Fecha {} es festivo.", fecha); return horariosDisponibles;
        }
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        if (diaSemana == DayOfWeek.SUNDAY) { // Asumiendo no domingos
            log.debug("Fecha {} es domingo.", fecha); return horariosDisponibles;
        }

        // 2. Determinar hora de fin
        LocalTime horaFinLaboral = (diaSemana == DayOfWeek.MONDAY || diaSemana == DayOfWeek.WEDNESDAY || diaSemana == DayOfWeek.FRIDAY)
                ? HORA_FIN_LABORAL_RESTRINGIDA : HORA_FIN_LABORAL_NORMAL;
        // Lógica Sábado si es diferente:
        // if (diaSemana == DayOfWeek.SATURDAY) { horaFinLaboral = LocalTime.of(12, 0); }
        log.debug("Horario laboral para {}: {} - {}", fecha, HORA_INICIO_LABORAL, horaFinLaboral);


        // 3. Obtener sesiones existentes (PROGRAMADA y CONFIRMADA)
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();
        // Usa el método del repositorio que busca por fecha Y estado
        List<Sesion> sesionesExistentes = sesionRepository.findByFechaBetweenAndEstadoIn(
                inicioDia,
                finDia,
                EnumSet.of(EstadoSesion.PROGRAMADA, EstadoSesion.CONFIRMADA)
        );
        log.debug("Sesiones existentes encontradas (Programadas/Confirmadas) para {}: {}", fecha, sesionesExistentes.size());

        // 4. Generar y filtrar horarios
        LocalTime horaPosible = HORA_INICIO_LABORAL;
        while (horaPosible.plus(DURACION_SESION_PREDET).isBefore(horaFinLaboral) || horaPosible.plus(DURACION_SESION_PREDET).equals(horaFinLaboral)) {
            LocalDateTime inicioPropuesto = fecha.atTime(horaPosible);
            LocalDateTime finPropuesto = inicioPropuesto.plus(DURACION_SESION_PREDET);

            // Verificar disponibilidad usando la lógica refinada
            boolean disponible = verificarDisponibilidadSlot(inicioPropuesto, finPropuesto, sesionesExistentes, null);

            if (disponible) {
                // No añadir horarios pasados si es hoy
                if (!fecha.isEqual(LocalDate.now()) || horaPosible.isAfter(LocalTime.now())) {
                    log.trace("Horario {} añadido.", horaPosible);
                    horariosDisponibles.add(horaPosible);
                } else {
                    log.trace("Horario {} descartado (pasado hoy).", horaPosible);
                }
            }

            horaPosible = horaPosible.plus(INTERVALO_AGENDAMIENTO);
        }
        log.debug("Horarios disponibles calculados para {}: {}", fecha, horariosDisponibles);
        return horariosDisponibles;
    }

    // --- MÉTODO AUXILIAR PARA VERIFICAR DISPONIBILIDAD (Reutiliza lógica) ---
    private boolean verificarDisponibilidadSlot(LocalDateTime inicioPropuesto, LocalDateTime finPropuesto, List<Sesion> sesionesExistentes, Long excludeId) {
        log.trace("Verificando slot: {} - {}, excludeId={}", inicioPropuesto, finPropuesto, excludeId);
        for (Sesion existente : sesionesExistentes) {
            // Ignorar la sesión excluida (para reprogramar)
            if (excludeId != null && existente.getId() != null && existente.getId().equals(excludeId)) {
                log.trace("Ignorando sesión existente ID {}", excludeId);
                continue;
            }
            // Usa la duración real de la sesión existente si está disponible, sino la predeterminada
            Duration duracionExistente = existente.getDuracion() != null ? existente.getDuracion() : DURACION_SESION_PREDET;
            LocalDateTime inicioOcupado = existente.getFecha().minus(TIEMPO_DESPLAZAMIENTO);
            LocalDateTime finOcupado = existente.getFecha().plus(duracionExistente).plus(TIEMPO_DESPLAZAMIENTO);

            // Comprobar solapamiento: Si el propuesto empieza antes de que termine el ocupado
            // Y el propuesto termina después de que empiece el ocupado
            if (inicioPropuesto.isBefore(finOcupado) && finPropuesto.isAfter(inicioOcupado)) {
                log.trace("Conflicto con sesión ID {}. Ocupado: {} - {}. Propuesto: {} - {}",
                        existente.getId(), inicioOcupado, finOcupado, inicioPropuesto, finPropuesto);
                return false; // Hay solapamiento
            }
        }
        log.trace("Slot {} - {} está LIBRE.", inicioPropuesto, finPropuesto);
        return true; // No hubo solapamientos
    }


    // --- IMPLEMENTACIÓN findSesionesPorEstado ---
    @Override
    @Transactional(readOnly = true)
    public List<Sesion> findSesionesPorEstado(EstadoSesion estado) {
        log.debug("Buscando sesiones con estado: {}", estado);
        // Necesitas el método findByEstadoOrderByFechaAsc en SesionRepository
        return sesionRepository.findByEstadoOrderByFechaAsc(estado);
    }

    // --- IMPLEMENTACIÓN findSesionesConfirmadasEntre ---
    @Override
    @Transactional(readOnly = true)
    public List<Sesion> findSesionesConfirmadasEntre(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando sesiones CONFIRMADAS entre {} y {}", inicio, fin);
        // Necesitas findByFechaBetweenAndEstadoInOrderByFechaAsc en SesionRepository
        // Necesitas findByFechaBetweenAndEstadoInOrderByFechaAsc en SesionRepository
        return sesionRepository.findByFechaBetweenAndEstadoInOrderByFechaAsc( // <-- Nombre corregido
                inicio,
                fin,
                EnumSet.of(EstadoSesion.CONFIRMADA)
        );
    }

    // --- MÉTODOS EXISTENTES (Revisados/Ajustados) ---

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth yearMonth) {
        if (pacienteId == null) { log.error("ID paciente nulo"); throw new IllegalArgumentException("ID paciente nulo"); }
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        log.debug("Buscando sesiones mes para pacienteId={} entre {} y {}", pacienteId, inicio, fin);
        // Necesitas findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraAsc en SesionRepository
        return sesionRepository.findByPacienteIdAndFechaBetweenOrderByFechaAsc(pacienteId, inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> buscarPendientesEntre(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando sesiones PROGRAMADAS/CONFIRMADAS entre {} y {}", inicio, fin);
        // Usa el método del repositorio que busca por fecha Y estado ordenado
        // Usa el método del repositorio que busca por fecha Y estado ordenado
        return sesionRepository.findByFechaBetweenAndEstadoInOrderByFechaAsc( // <-- Nombre corregido
                inicio,
                fin,
                EnumSet.of(EstadoSesion.PROGRAMADA, EstadoSesion.CONFIRMADA)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> buscarPendientesAntes(LocalDateTime fecha) {
        log.debug("Buscando sesiones PROGRAMADAS/CONFIRMADAS antes de {}", fecha);
        // Necesitas findByFechaHoraBeforeAndEstadoIn en SesionRepository
        return sesionRepository.findByFechaBeforeAndEstadoIn(
                fecha, // El límite superior es la fecha dada
                EnumSet.of(EstadoSesion.PROGRAMADA, EstadoSesion.CONFIRMADA)
        );
    }

    @Override
    @Transactional
    public Optional<Sesion> confirmarSesion(Long sesionId) {
        if (sesionId == null) { log.error("ID nulo"); throw new IllegalArgumentException("ID nulo"); }
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            // CORRECCIÓN: Usa el Enum EstadoSesion directamente
            if (sesion.getEstado() == PROGRAMADA) {
                sesion.setEstado(CONFIRMADA);
                Sesion sesionConfirmada = sesionRepository.save(sesion);
                log.info("Sesión ID {} confirmada.", sesionId);
                // Enviar notificación (Fase 4)
                try {
                    notificacionService.enviarConfirmacionPaciente(sesionConfirmada,
                            "Se confirma su cita para el dia "
                            + sesion.getFecha().format(DateTimeFormatter.ofPattern("DD/MM/YYYY 'a las' HH:mm"))
                            + " en "
                            + sesion.getLugar()
                            + ".\\n Gracias por la oportunidad!."
                            + "\\n Hasta entonces !"
                            );
                    log.info("Notificación de confirmación enviada para sesión ID {}", sesionConfirmada.getId());
                } catch (Exception e) {
                    log.error("Error al enviar notificación de confirmación para sesión {}: {}", sesionConfirmada.getId(), e.getMessage(), e);
                }
                return Optional.of(sesionConfirmada);
            } else {
                log.warn("Intento de confirmar sesión ID {} que no estaba PROGRAMADA (estado: {})", sesionId, sesion.getEstado());
            }
        } else {
            log.warn("Sesión no encontrada para confirmar: ID {}", sesionId);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void cancelarSesion(Long sesionId) {
        if (sesionId == null) { log.error("ID nulo"); throw new IllegalArgumentException("ID nulo"); }
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            // CORRECCIÓN: Usa el Enum EstadoSesion directamente
            if (sesion.getEstado() == PROGRAMADA || sesion.getEstado() == CONFIRMADA) {
                sesion.setEstado(CANCELADA);
                sesionRepository.save(sesion);
                log.info("Sesión ID {} cancelada.", sesionId);
                // Enviar notificación (Fase 4)
                try {
                    String motivo = "Cancelada por el médico."; // Añadir motivo real si se pasa


                    notificacionService.enviarRecordatorioPaciente(sesion);


                    log.info("Notificación de cancelación enviada para sesión ID {}", sesion.getId());
                } catch (Exception e) {
                    log.error("Error al enviar notificación de cancelación para sesión {}: {}", sesion.getId(), e.getMessage(), e);
                }
            } else if (sesion.getEstado() == CANCELADA) {
                log.warn("Sesión ID {} ya estaba cancelada.", sesionId);
                // No hacemos nada si ya estaba cancelada
            } else {
                log.warn("Intento de cancelar sesión ID {} en estado inesperado: {}", sesionId, sesion.getEstado());
            }
        } else {
            log.warn("Sesión no encontrada para cancelar: ID {}", sesionId);
            // Considera lanzar EntityNotFoundException si prefieres
        }
    }

    // --- AJUSTE estaDisponible (Usa el método auxiliar) ---
    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(LocalDateTime inicio, Duration duracion, Long excludeId) {
        if (inicio == null || duracion == null) { log.error("Parámetros nulos"); throw new IllegalArgumentException("Parámetros nulos"); }
        LocalDateTime fin = inicio.plus(duracion);

        // 1. Validaciones básicas (horario, festivo, pasado)
        LocalDate fecha = inicio.toLocalDate();
        LocalTime horaInicio = inicio.toLocalTime();
        if (inicio.isBefore(LocalDateTime.now())) { log.warn("Intento de verificar disponibilidad en el pasado: {}", inicio); return false; }
        if (FestivosColombia.esFestivo(fecha)) { log.debug("Fecha {} es festivo.", fecha); return false; }
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        if (diaSemana == DayOfWeek.SUNDAY) { log.debug("Fecha {} es domingo.", fecha); return false; }

        LocalTime horaFinLaboral = (diaSemana == DayOfWeek.MONDAY || diaSemana == DayOfWeek.WEDNESDAY || diaSemana == DayOfWeek.FRIDAY)
                ? HORA_FIN_LABORAL_RESTRINGIDA : HORA_FIN_LABORAL_NORMAL;
        // if (diaSemana == DayOfWeek.SATURDAY) { horaFinLaboral = LocalTime.of(12, 0); }
        if (horaInicio.isBefore(HORA_INICIO_LABORAL) || fin.toLocalTime().isAfter(horaFinLaboral) || (fin.toLocalTime().equals(horaFinLaboral) && !horaInicio.equals(horaFinLaboral.minus(duracion)))) {
            log.debug("Horario {} - {} fuera de horas laborales.", inicio, fin); return false;
        }

        // 2. Comprobar solapamiento usando el método auxiliar
        LocalDateTime inicioBusqueda = inicio.minus(DURACION_SESION_PREDET).minus(TIEMPO_DESPLAZAMIENTO);
        LocalDateTime finBusqueda = fin.plus(DURACION_SESION_PREDET).plus(TIEMPO_DESPLAZAMIENTO);
        List<Sesion> sesionesCercanas = sesionRepository.findByFechaBetweenAndEstadoIn(
                inicioBusqueda, finBusqueda, EnumSet.of(EstadoSesion.PROGRAMADA, EstadoSesion.CONFIRMADA));

        return verificarDisponibilidadSlot(inicio, fin, sesionesCercanas, excludeId);
    }
    // Sobrecarga sin excludeId (se mantiene igual)
    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(LocalDateTime inicio, Duration duracion) {
        return estaDisponible(inicio, duracion, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia) {
        if (pacienteId == null || dia == null) { log.error("Parámetros nulos"); throw new IllegalArgumentException("Parámetros nulos");}
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.plusDays(1).atStartOfDay(); // Mejor hasta el inicio del día siguiente
        log.debug("Buscando sesiones día para pacienteId={} en día {}", pacienteId, dia);
        // Necesitas findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraAsc en el repo
        return sesionRepository.findByPacienteIdAndFechaBetweenOrderByFechaAsc(pacienteId, inicio, fin);
    }

    // --- AJUSTE guardarSesion (Añade Notificación Programación y merge) ---
    @Override
    @Transactional
    public void guardarSesion(Sesion sesion) {
        if (sesion == null || sesion.getPaciente() == null || sesion.getPaciente().getId() == null) {
            log.error("Sesión inválida al guardar: sesión o paciente nulo/sin ID. Sesion: {}", sesion);
            throw new IllegalArgumentException("Sesión o paciente inválido para guardar");
        }

        // Re-adjuntar paciente si está detached
        Paciente paciente = sesion.getPaciente();
        if (!entityManager.contains(paciente)) {
            log.debug("Paciente ID {} está detached. Haciendo merge.", paciente.getId());
            try {
                paciente = entityManager.merge(paciente);
                sesion.setPaciente(paciente);
            } catch (Exception e) {
                log.error("Error al hacer merge del paciente ID {}: {}", sesion.getPaciente().getId(), e.getMessage(), e);
                // Decide cómo manejar esto: lanzar excepción, notificar error?
                throw new RuntimeException("Error al asociar el paciente a la sesión.", e);
            }
        }

        boolean esNueva = sesion.getId() == null;
        if (esNueva) {
            sesion.setEstado(PROGRAMADA); // Asegura estado inicial
            // Asegura una duración si no viene del frontend
            if (sesion.getDuracion() == null) {
                sesion.setDuracion(DURACION_SESION_PREDET);
            }
            log.info("Guardando NUEVA sesión para paciente ID {} en fecha {}", paciente.getId(), sesion.getFecha());
        } else {
            log.info("Actualizando sesión ID {} para paciente ID {} en fecha {}", sesion.getId(), paciente.getId(), sesion.getFecha());
        }

        // Antes de guardar, verificar disponibilidad por si acaso
        if (!estaDisponible(sesion.getFecha(), sesion.getDuracion() != null ? sesion.getDuracion() : DURACION_SESION_PREDET, sesion.getId())) {
            log.error("Intento de guardar sesión ID {} en horario NO disponible: {}", sesion.getId(), sesion.getFecha());
            throw new IllegalStateException("El horario seleccionado ya no está disponible."); // Lanza excepción
        }


        Sesion sesionGuardada = sesionRepository.save(sesion);
        log.debug("Sesión guardada/actualizada con ID {}", sesionGuardada.getId());

        // Enviar notificación de programación SOLO si es una sesión NUEVA
        if (esNueva) {
            log.debug("Intentando enviar notificación de programación para sesión ID {}", sesionGuardada.getId());
            try {
                notificacionService.enviarNotificacionProgramacionMedico(sesionGuardada);
                log.info("Notificación de programación enviada para sesión ID {}", sesionGuardada.getId());
            } catch (Exception e) {
                log.error("Error al enviar notificación de programación para sesión {}: {}", sesionGuardada.getId(), e.getMessage(), e);
            }
        }
    }


    // --- AJUSTE reprogramarSesion (Añade Notificación y merge) ---
    @Override
    @Transactional
    public Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFechaHora, Duration duracion) {
        if (sesionId == null || nuevaFechaHora == null || duracion == null) { log.error("Parámetros nulos"); throw new IllegalArgumentException("Parámetros nulos");}

        // Verifica disponibilidad ANTES de buscar la sesión
        if (!estaDisponible(nuevaFechaHora, duracion, sesionId)) {
            log.warn("Horario no disponible para reprogramar sesión ID {} a {}", sesionId, nuevaFechaHora);
            return Optional.empty(); // O lanzar HorarioNoDisponibleException
        }

        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            if (sesion.getEstado() == PROGRAMADA || sesion.getEstado() == CONFIRMADA) {
                // Re-adjuntar paciente
                if (!entityManager.contains(sesion.getPaciente())) {
                    log.debug("Paciente ID {} detached en reprogramación. Merge.", sesion.getPaciente().getId());
                    try {
                        sesion.setPaciente(entityManager.merge(sesion.getPaciente()));
                    } catch (Exception e) {
                        log.error("Error al hacer merge del paciente ID {} en reprogramación: {}", sesion.getPaciente().getId(), e.getMessage(), e);
                        throw new RuntimeException("Error al asociar el paciente al reprogramar.", e);
                    }
                }
                LocalDateTime fechaAnterior = sesion.getFecha();
                sesion.setFecha(nuevaFechaHora);
                sesion.setDuracion(duracion);
                sesion.setEstado(PROGRAMADA); // Volver a programada requiere reconfirmación
                Sesion reprogramada = sesionRepository.save(sesion);
                log.info("Sesión ID {} reprogramada de {} a {}.", sesionId, fechaAnterior, nuevaFechaHora);

                // Enviar notificación (Fase 4)
                try {
                    log.debug("Intentando enviar notificación de reprogramación para sesión ID {}", reprogramada.getId());
                    // Necesitarás añadir enviarReprogramacionCita a NotificacionService
                    notificacionService.enviarReprogramacionCita(reprogramada, fechaAnterior);
                    log.info("Notificación de reprogramación enviada para sesión ID {}", reprogramada.getId());
                } catch (Exception e) {
                    log.error("Error enviando notificación de reprogramación para {}: {}", sesionId, e.getMessage(), e);
                }
                return Optional.of(reprogramada);
            } else {
                log.warn("Intento de reprogramar sesión ID {} en estado inesperado: {}", sesionId, sesion.getEstado());
            }
        } else {
            log.warn("Sesión no encontrada para reprogramar: ID {}", sesionId);
        }
        return Optional.empty();
    }


    // --- Métodos del Repositorio a añadir/verificar en SesionRepository.java ---
    /*
     * List<Sesion> findByPacienteIdAndFechaHoraBetweenOrderByFechaHoraAsc(Long pacienteId, LocalDateTime start, LocalDateTime end);
     * List<Sesion> findByFechaBetweenAndEstadoInOrderByFechaHoraAsc(LocalDateTime start, LocalDateTime end, Set<EstadoSesion> estados);
     * List<Sesion> findByFechaBetweenAndEstadoIn(LocalDateTime start, LocalDateTime end, Set<EstadoSesion> estados); // Ya debería existir por tu código
     * List<Sesion> findByEstadoOrderByFechaAsc(EstadoSesion estado);
     * List<Sesion> findByFechaHoraBeforeAndEstadoIn(LocalDateTime fecha, Set<EstadoSesion> estados);
     */

    // --- Métodos de NotificacionService a añadir/verificar en NotificacionService.java ---
    /*
     * void enviarProgramacionCita(Sesion sesion);
     * void enviarConfirmacionCita(Sesion sesion);
     * void enviarCancelacionCita(Sesion sesion, String motivo);
     * void enviarReprogramacionCita(Sesion sesion, LocalDateTime fechaAnterior); // Opcional pasar fecha anterior
     */

}