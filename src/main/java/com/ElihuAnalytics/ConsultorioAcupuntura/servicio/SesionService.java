package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.EstadoSesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;

import java.time.*;
import java.util.List;
import java.util.Optional;

public interface SesionService {
    List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth yearMonth);
    List<Sesion> buscarPendientesEntre(LocalDateTime inicio, LocalDateTime fin);
    Optional<Sesion> confirmarSesion(Long sesionId);
    void cancelarSesion(Long sesionId);
    void guardarSesion(Sesion sesion);
    List<Sesion> buscarPendientesAntes(LocalDateTime fecha);
    boolean estaDisponible(LocalDateTime fecha, Duration duracion);
    boolean estaDisponible(LocalDateTime fecha, Duration duracion, Long excludeId);
    List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia);
    Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFecha, Duration duracion);
    // --- NUEVOS MÉTODOS REQUERIDOS ---

    /**
     * Calcula y devuelve una lista de horas de inicio disponibles para agendar
     * una sesión en una fecha específica, considerando el horario laboral,
     * festivos, restricciones horarias y sesiones existentes + tiempo de desplazamiento.
     *
     * @param fecha La fecha para la cual calcular la disponibilidad.
     * @return Lista de LocalTime disponibles para iniciar una sesión.
     */
    List<LocalTime> getHorasDisponibles(LocalDate fecha);

    /**
     * Busca todas las sesiones que se encuentran en un estado específico.
     * Útil para el panel del médico (ej. listar PROGRAMADA).
     *
     * @param estado El EstadoSesion a buscar.
     * @return Lista de sesiones en el estado especificado.
     */
    List<Sesion> findSesionesPorEstado(EstadoSesion estado);

    /**
     * Busca sesiones confirmadas dentro de un rango de fechas y horas.
     * Útil para el scheduler de recordatorios.
     *
     * @param inicio Fecha y hora de inicio del rango.
     * @param fin Fecha y hora de fin del rango.
     * @return Lista de sesiones confirmadas en ese intervalo.
     */
    List<Sesion> findSesionesConfirmadasEntre(LocalDateTime inicio, LocalDateTime fin); // Cambiado nombre para claridad
}