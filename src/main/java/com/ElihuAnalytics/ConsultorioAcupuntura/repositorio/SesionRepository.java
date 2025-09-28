package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByPacienteId(Long pacienteId);
    List<Sesion> findByPacienteIdAndFechaBetween(Long pacienteId, LocalDateTime desde, LocalDateTime hasta);
    // Para validar disponibilidad global (todas las sesiones PROGRAMADAS)
    long countByEstadoAndFechaBetween(Sesion.EstadoSesion estado, LocalDateTime desde, LocalDateTime hasta);
    // Para reprogramación (traer sesiones en ventana y filtrar por id en servicio)
    List<Sesion> findByEstadoAndFechaBetween(Sesion.EstadoSesion estado, LocalDateTime desde, LocalDateTime hasta);
}

