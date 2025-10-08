package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByPacienteIdAndFechaBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin);
    List<Sesion> findByFechaBetweenAndEstadoIn(LocalDateTime inicio, LocalDateTime fin, List<Sesion.EstadoSesion> estados);
    List<Sesion> findByFechaBeforeAndEstadoIn(LocalDateTime fecha, List<Sesion.EstadoSesion> estados);
}