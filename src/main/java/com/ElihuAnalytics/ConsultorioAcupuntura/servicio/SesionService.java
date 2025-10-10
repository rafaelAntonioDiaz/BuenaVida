package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
}