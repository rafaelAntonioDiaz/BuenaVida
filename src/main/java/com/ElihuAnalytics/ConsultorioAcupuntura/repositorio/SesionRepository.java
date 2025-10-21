package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.EstadoSesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Repositorio para gestionar sesiones (citas) en la base de datos.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    /**
     * Busca sesiones de un paciente en un rango de fechas.
     * @param pacienteId ID del paciente
     * @param inicio Fecha de inicio
     * @param fin Fecha de fin
     * @return Lista de sesiones
     */
    List<Sesion> findByPacienteIdAndFechaBetween(
            Long pacienteId,
            LocalDateTime inicio,
            LocalDateTime fin);
    /**
     * Busca sesiones en un rango de fechas con estado PROGRAMADA o CONFIRMADA.
     * @param inicio Fecha de inicio
     * @param fin Fecha de fin
     * @param estados Lista de estados
     * @return Lista de sesiones
     */
    List<Sesion> findByFechaBetweenAndEstadoIn(
            LocalDateTime inicio,
            LocalDateTime fin,
            List<Sesion.EstadoSesion> estados);
// --- MÉTODOS REQUERIDOS ---

    // Usado por obtenerSesionesPorPacienteYMes y obtenerSesionesPorPacienteYDia
    // --- CORRECCIÓN ---
// Cambiado OrderByFechaHoraAsc a OrderByFechaAsc
    List<Sesion> findByPacienteIdAndFechaBetweenOrderByFechaAsc(
            Long pacienteId,
            LocalDateTime start, LocalDateTime end);
// --- FIN CORRECCIÓN ---

    // Usado por buscarPendientesEntre y findSesionesConfirmadasEntre (con diferente Set de estados)
// --- CORRECCIÓN ---
// Cambiado OrderByFechaHoraAsc a OrderByFechaAsc
    List<Sesion> findByFechaBetweenAndEstadoInOrderByFechaAsc(
            LocalDateTime start,
            LocalDateTime end,
            Set<EstadoSesion> estados);
    // --- FIN CORRECCIÓN ---    // Usado por getHorasDisponibles y estaDisponible (no necesita orden)
    List<Sesion> findByFechaBetweenAndEstadoIn(
            LocalDateTime start,
            LocalDateTime end,
            Set<EstadoSesion> estados);

    // Usado por findSesionesPorEstado
    List<Sesion> findByEstadoOrderByFechaAsc(
            EstadoSesion estado);

    // Usado por buscarPendientesAntes
    List<Sesion> findByFechaBeforeAndEstadoIn(
            LocalDateTime fecha,
            Set<EstadoSesion> estados);


}