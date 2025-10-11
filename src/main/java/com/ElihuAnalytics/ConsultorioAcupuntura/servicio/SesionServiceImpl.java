package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.SesionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar sesiones (citas) en el consultorio.
 */
@Service
public class SesionServiceImpl implements SesionService {

    private static final Logger log = LoggerFactory.getLogger(SesionServiceImpl.class);
    private static final Duration DESPLAZAMIENTO = Duration.ofMinutes(30); // 30 minutos para desplazamiento

    private final SesionRepository sesionRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public SesionServiceImpl(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth yearMonth) {
        if (pacienteId == null) {
            log.error("pacienteId es nulo en obtenerSesionesPorPacienteYMes");
            throw new IllegalArgumentException("El ID del paciente no puede ser nulo");
        }
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        log.debug("Buscando sesiones para pacienteId={} entre {} y {}", pacienteId, inicio, fin);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> buscarPendientesEntre(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando sesiones pendientes entre {} y {}", inicio, fin);
        return sesionRepository.findByFechaBetweenAndEstadoIn(
                inicio,
                fin,
                Arrays.asList(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> buscarPendientesAntes(LocalDateTime fecha) {
        log.debug("Buscando sesiones pendientes antes de {}", fecha);
        return sesionRepository.findByFechaBetweenAndEstadoIn(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                fecha,
                Arrays.asList(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA)
        );
    }

    @Override
    @Transactional
    public Optional<Sesion> confirmarSesion(Long sesionId) {
        if (sesionId == null) {
            log.error("sesionId es nulo en confirmarSesion");
            throw new IllegalArgumentException("El ID de la sesión no puede ser nulo");
        }
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (!sesionOpt.isPresent()) {
            log.warn("Sesión no encontrada: id={}", sesionId);
            return Optional.empty();
        }
        Sesion sesion = sesionOpt.get();
        if (sesion.getEstado() != Sesion.EstadoSesion.PROGRAMADA) {
            log.warn("Sesión no está en estado PROGRAMADA: id={}, estado={}", sesionId, sesion.getEstado());
            throw new IllegalStateException("Solo se pueden confirmar sesiones programadas");
        }
        sesion.setEstado(Sesion.EstadoSesion.CONFIRMADA);
        log.info("Confirmando sesión: id={}", sesionId);
        return Optional.of(sesionRepository.save(sesion));
    }

    @Override
    @Transactional
    public void cancelarSesion(Long sesionId) {
        if (sesionId == null) {
            log.error("sesionId es nulo en cancelarSesion");
            throw new IllegalArgumentException("El ID de la sesión no puede ser nulo");
        }
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (!sesionOpt.isPresent()) {
            log.warn("Sesión no encontrada: id={}", sesionId);
            throw new IllegalStateException("Sesión no encontrada");
        }
        Sesion sesion = sesionOpt.get();
        if (sesion.getEstado() == Sesion.EstadoSesion.CANCELADA) {
            log.warn("Sesión ya cancelada: id={}", sesionId);
            return;
        }
        sesion.setEstado(Sesion.EstadoSesion.CANCELADA);
        log.info("Cancelando sesión: id={}", sesionId);
        sesionRepository.save(sesion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(LocalDateTime inicio, Duration duracion) {
        return estaDisponible(inicio, duracion, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(LocalDateTime inicio, Duration duracion, Long excludeId) {
        if (inicio == null || duracion == null) {
            log.error("Parámetros inválidos en estaDisponible: inicio={}, duracion={}", inicio, duracion);
            throw new IllegalArgumentException("Fecha y duración no pueden ser nulos");
        }
        LocalDateTime finCita = inicio.plus(duracion);
        LocalDateTime finConDesplazamiento = finCita.plus(DESPLAZAMIENTO);
        List<Sesion> sesiones = buscarPendientesEntre(inicio.minus(DESPLAZAMIENTO), finConDesplazamiento);
        for (Sesion sesion : sesiones) {
            if (excludeId != null && sesion.getId() != null && sesion.getId().equals(excludeId)) {
                continue;
            }
            LocalDateTime sesionInicio = sesion.getFecha();
            LocalDateTime sesionFin = sesionInicio.plus(sesion.getDuracion());
            if (inicio.isBefore(sesionFin.plus(DESPLAZAMIENTO)) && finCita.plus(DESPLAZAMIENTO).isAfter(sesionInicio)) {
                log.warn("Conflicto de horario detectado: inicio={}, fin={}, sesion_existente=[id={}, inicio={}, fin={}]",
                        inicio, finCita, sesion.getId(), sesionInicio, sesionFin);
                return false;
            }
        }
        log.debug("Horario disponible: inicio={}, fin={}", inicio, finCita);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia) {
        if (pacienteId == null || dia == null) {
            log.error("Parámetros inválidos en obtenerSesionesPorPacienteYDia: pacienteId={}, dia={}", pacienteId, dia);
            throw new IllegalArgumentException("Paciente ID y día no pueden ser nulos");
        }
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(23, 59, 59);
        log.debug("Buscando sesiones para pacienteId={} en día {}", pacienteId, dia);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    @Override
    @Transactional
    public void guardarSesion(Sesion sesion) {
        if (sesion == null || sesion.getPaciente() == null || sesion.getPaciente().getId() == null) {
            log.error("Sesión inválida: sesion={}, paciente={}", sesion, sesion != null ? sesion.getPaciente() : null);
            throw new IllegalArgumentException("Sesión o paciente inválido");
        }
        Paciente paciente = sesion.getPaciente();
        log.info("Mergiendo paciente: id={}, username={}", paciente.getId(), paciente.getUsername());
        Paciente pacientePersistido = entityManager.merge(paciente);
        sesion.setPaciente(pacientePersistido);
        log.info("Guardando sesión: id={}, paciente_id={}, fecha={}, motivo={}",
                sesion.getId(), pacientePersistido.getId(), sesion.getFecha(), sesion.getMotivo());
        sesionRepository.save(sesion);
    }

    @Override
    @Transactional
    public Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFecha, Duration duracion) {
        if (sesionId == null || nuevaFecha == null || duracion == null) {
            log.error("Parámetros inválidos en reprogramarSesion: sesionId={}, nuevaFecha={}, duracion={}",
                    sesionId, nuevaFecha, duracion);
            throw new IllegalArgumentException("Parámetros no pueden ser nulos");
        }
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (!sesionOpt.isPresent()) {
            log.warn("Sesión no encontrada: id={}", sesionId);
            return Optional.empty();
        }
        Sesion sesion = sesionOpt.get();
        if (!estaDisponible(nuevaFecha, duracion, sesionId)) {
            log.warn("Horario no disponible para reprogramar: id={}, nuevaFecha={}", sesionId, nuevaFecha);
            return Optional.empty();
        }
        Paciente pacientePersistido = entityManager.merge(sesion.getPaciente());
        sesion.setPaciente(pacientePersistido);
        sesion.setFecha(nuevaFecha);
        sesion.setDuracion(duracion);
        sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
        log.info("Reprogramando sesión: id={}, nuevaFecha={}, paciente_id={}",
                sesionId, nuevaFecha, pacientePersistido.getId());
        return Optional.of(sesionRepository.save(sesion));
    }
}
