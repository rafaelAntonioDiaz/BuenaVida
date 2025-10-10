package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.SesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar sesiones (citas) en el consultorio.
 */
@Service
public class SesionServiceImpl implements SesionService {

    private static final Duration DESPLAZAMIENTO = Duration.ofMinutes(30); // 30 minutos para desplazamiento después de la cita

    private final SesionRepository sesionRepository;

    public SesionServiceImpl(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    /**
     * Obtiene las sesiones de un paciente en un mes específico.
     * @param pacienteId ID del paciente
     * @param yearMonth Mes y año a consultar
     * @return Lista de sesiones
     */
    @Override
    public List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth yearMonth) {
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    /**
     * Busca sesiones pendientes (PROGRAMADA o CONFIRMADA) en un rango de fechas.
     * @param inicio Fecha de inicio
     * @param fin Fecha de fin
     * @return Lista de sesiones
     */
    @Override
    public List<Sesion> buscarPendientesEntre(LocalDateTime inicio, LocalDateTime fin) {
        return sesionRepository.findByFechaBetweenAndEstadoIn(inicio, fin, List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA));
    }

    /**
     * Confirma una sesión cambiando su estado a CONFIRMADA.
     * @param sesionId ID de la sesión
     * @return Sesión confirmada o vacía si no se puede confirmar
     */
    @Override
    @Transactional
    public Optional<Sesion> confirmarSesion(Long sesionId) {
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            if (sesion.getEstado() == Sesion.EstadoSesion.PROGRAMADA) {
                sesion.setEstado(Sesion.EstadoSesion.CONFIRMADA);
                sesionRepository.save(sesion);
                return Optional.of(sesion);
            }
        }
        return Optional.empty();
    }

    /**
     * Cancela una sesión cambiando su estado a CANCELADA.
     * @param sesionId ID de la sesión
     */
    @Override
    @Transactional
    public void cancelarSesion(Long sesionId) {
        sesionRepository.findById(sesionId).ifPresent(sesion -> {
            sesion.setEstado(Sesion.EstadoSesion.CANCELADA);
            sesionRepository.save(sesion);
        });
    }

    /**
     * Guarda una sesión en la base de datos.
     * @param sesion Sesión a guardar
     */
    @Override
    @Transactional
    public void guardarSesion(Sesion sesion) {
        sesion.setDuracion(Duration.ofHours(1)); // Asegurar duración de 1 hora
        sesionRepository.save(sesion);
    }

    /**
     * Busca sesiones pendientes antes de una fecha.
     * @param fecha Fecha límite
     * @return Lista de sesiones
     */
    @Override
    public List<Sesion> buscarPendientesAntes(LocalDateTime fecha) {
        return sesionRepository.findByFechaBeforeAndEstadoIn(fecha, List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA));
    }

    /**
     * Verifica si un horario está disponible para una cita (sin considerar una sesión específica).
     * @param fecha Fecha y hora de inicio
     * @param duracion Duración de la cita
     * @return true si el horario está disponible
     */
    @Override
    public boolean estaDisponible(LocalDateTime fecha, Duration duracion) {
        return estaDisponible(fecha, duracion, null);
    }

    /**
     * Verifica si un horario está disponible, excluyendo una sesión específica (para reprogramaciones).
     * @param fecha Fecha y hora de inicio
     * @param duracion Duración de la cita
     * @param excludeId ID de la sesión a excluir (puede ser null)
     * @return true si el horario está disponible
     */
    @Override
    public boolean estaDisponible(LocalDateTime fecha, Duration duracion, Long excludeId) {
        LocalDateTime finCita = fecha.plus(duracion);
        LocalDateTime finDesplazamiento = finCita.plus(DESPLAZAMIENTO);
        List<Sesion> sesiones = sesionRepository.findByFechaBetweenAndEstadoIn(
                fecha, finDesplazamiento, List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA)
        );
        return sesiones.stream()
                .filter(s -> excludeId == null || !s.getId().equals(excludeId))
                .noneMatch(s -> s.getFecha().isBefore(finDesplazamiento) && s.getFecha().plus(s.getDuracion()).isAfter(fecha));
    }

    /**
     * Obtiene las sesiones de un paciente en un día específico.
     * @param pacienteId ID del paciente
     * @param dia Día a consultar
     * @return Lista de sesiones
     */
    @Override
    public List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia) {
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(23, 59, 59);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    /**
     * Reprograma una sesión a una nueva fecha si está disponible.
     * @param sesionId ID de la sesión
     * @param nuevaFecha Nueva fecha y hora
     * @param duracion Duración de la cita
     * @return Sesión reprogramada o vacía si no se puede reprogramar
     */
    @Override
    @Transactional
    public Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFecha, Duration duracion) {
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            if (sesion.getEstado() == Sesion.EstadoSesion.PROGRAMADA || sesion.getEstado() == Sesion.EstadoSesion.CONFIRMADA) {
                if (estaDisponible(nuevaFecha, duracion, sesionId)) {
                    sesion.setFecha(nuevaFecha);
                    sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
                    sesion.setDuracion(duracion);
                    sesionRepository.save(sesion);
                    return Optional.of(sesion);
                } else {
                    throw new IllegalStateException("La nueva fecha no está disponible.");
                }
            }
        }
        return Optional.empty();
    }
}