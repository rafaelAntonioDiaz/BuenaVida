package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.SesionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar sesiones de citas.
 */
@Service
public class SesionService {

    private static final Logger log = LoggerFactory.getLogger(SesionService.class);
    private final SesionRepository sesionRepository;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    public Sesion guardarSesion(Sesion sesion) {
        return sesionRepository.save(sesion);
    }

    public List<Sesion> obtenerSesionesPorPacienteId(Long pacienteId) {
        return sesionRepository.findByPacienteId(pacienteId);
    }

    public List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth mes) {
        LocalDateTime desde = mes.atDay(1).atStartOfDay();
        LocalDateTime hasta = mes.atEndOfMonth().atTime(LocalTime.MAX);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, desde, hasta);
    }

    public List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia) {
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.atTime(LocalTime.MAX);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, desde, hasta)
                .stream()
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .collect(Collectors.toList());
    }

    public boolean estaDisponible(LocalDateTime inicio, Duration duracion) {
        LocalDateTime ventanaInicio = inicio.minusHours(1);
        LocalDateTime ventanaFin = inicio.plus(duracion).plusHours(1);
        long conflictos = sesionRepository.countByEstadoAndFechaBetween(
                Sesion.EstadoSesion.PROGRAMADA, ventanaInicio, ventanaFin
        );
        return conflictos == 0;
    }

    public boolean estaDisponibleExcluyendo(LocalDateTime inicio, Duration duracion, Long excluirSesionId) {
        LocalDateTime ventanaInicio = inicio.minusHours(1);
        LocalDateTime ventanaFin = inicio.plus(duracion).plusHours(1);
        return sesionRepository.findByEstadoAndFechaBetween(Sesion.EstadoSesion.PROGRAMADA, ventanaInicio, ventanaFin)
                .stream()
                .noneMatch(s -> !s.getId().equals(excluirSesionId));
    }

    public Optional<Sesion> cancelarSesion(Long sesionId) {
        return sesionRepository.findById(sesionId).map(s -> {
            s.setEstado(Sesion.EstadoSesion.CANCELADA);
            return sesionRepository.save(s);
        });
    }

    public Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFecha, Duration duracion) {
        return sesionRepository.findById(sesionId).flatMap(s -> {
            if (!estaDisponibleExcluyendo(nuevaFecha, duracion, s.getId())) {
                return Optional.empty();
            }
            s.setFecha(nuevaFecha);
            s.setEstado(Sesion.EstadoSesion.PROGRAMADA);
            return Optional.of(sesionRepository.save(s));
        });
    }

    public Optional<Sesion> confirmarSesion(Long sesionId) {
        return sesionRepository.findById(sesionId).map(s -> {
            if (s.getEstado() != Sesion.EstadoSesion.PROGRAMADA) {
                log.warn("Sesión {} no está en estado PROGRAMADA, no se puede confirmar.", s.getId());
                return null;
            }
            s.setEstado(Sesion.EstadoSesion.CONFIRMADA); // Usa estado CONFIRMADA
            try {
                return sesionRepository.save(s);
            } catch (Exception e) {
                log.error("Error al guardar sesión confirmada {}: {}", s.getId(), e.getMessage(), e);
                return null;
            }
        });
    }

    public Optional<Sesion> buscarPorId(Long sesionId) {
        return sesionRepository.findById(sesionId);
    }

    public List<Sesion> buscarPendientesEntre(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        }
        LocalDateTime ini = desde.isAfter(hasta) ? hasta : desde;
        LocalDateTime fin = desde.isAfter(hasta) ? desde : hasta;

        return sesionRepository
                .findByEstadoAndFechaBetween(Sesion.EstadoSesion.PROGRAMADA, ini, fin)
                .stream()
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList());
    }
}