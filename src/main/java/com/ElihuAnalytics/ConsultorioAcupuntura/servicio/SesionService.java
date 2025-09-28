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

    // Disponibilidad con ventana de desplazamiento
    public boolean estaDisponible(LocalDateTime inicio, Duration duracion) {
        LocalDateTime ventanaInicio = inicio.minusHours(1);
        LocalDateTime ventanaFin = inicio.plus(duracion).plusHours(1);
        long conflictos = sesionRepository.countByEstadoAndFechaBetween(
                Sesion.EstadoSesion.PROGRAMADA, ventanaInicio, ventanaFin
        );
        return conflictos == 0;
    }

    // Disponibilidad excluyendo una sesión (para reprogramar)
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
    //Confirmar sesión (implementación defensiva sin romper tu modelo)
    public Optional<Sesion> confirmarSesion(Long sesionId) {
        return sesionRepository.findById(sesionId).map(s -> {
            boolean actualizado = false;

            // Intentar marcar flags de confirmación si existen
            try { s.getClass().getMethod("setConfirmada", boolean.class).invoke(s, true); actualizado = true; } catch (Exception ignore) {}
            try { s.getClass().getMethod("setConfirmado", Boolean.class).invoke(s, Boolean.TRUE); actualizado = true; } catch (Exception ignore) {}
            try { s.getClass().getMethod("setConfirmado", boolean.class).invoke(s, true); actualizado = true; } catch (Exception ignore) {}

            // Intentar setear timestamp de confirmación si existe
            LocalDateTime ahora = LocalDateTime.now();
            try { s.getClass().getMethod("setConfirmadaEn", LocalDateTime.class).invoke(s, ahora); actualizado = true; } catch (Exception ignore) {}
            try { s.getClass().getMethod("setConfirmadoEn", LocalDateTime.class).invoke(s, ahora); actualizado = true; } catch (Exception ignore) {}
            try { s.getClass().getMethod("setFechaConfirmacion", LocalDateTime.class).invoke(s, ahora); actualizado = true; } catch (Exception ignore) {}

            if (!actualizado) {
                log.debug("Sesión {} confirmada (sin campos específicos de confirmación en la entidad).", s.getId());
            }
            return sesionRepository.save(s);
        });
    }

    // NUEVO: Enviar recordatorio (placeholder; conecta tu servicio real de notificaciones aquí)
    public void enviarRecordatorioSesion(Long sesionId) {
        sesionRepository.findById(sesionId).ifPresent(s -> {
            log.info("Recordatorio preparado para sesión {} · {} {}",
                    s.getId(),
                    s.getFecha().toLocalDate(),
                    s.getFecha().toLocalTime());
        });
    }
    public Optional<Sesion> buscarPorId(Long sesionId) {
        return sesionRepository.findById(sesionId);
    }

    // NUEVO: Buscar sesiones PROGRAMADAS entre dos fechas (pendientes)
    public List<Sesion> buscarPendientesEntre(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        }
        // Normalizar si vienen invertidas
        LocalDateTime ini = desde.isAfter(hasta) ? hasta : desde;
        LocalDateTime fin = desde.isAfter(hasta) ? desde : hasta;

        return sesionRepository
                .findByEstadoAndFechaBetween(Sesion.EstadoSesion.PROGRAMADA, ini, fin)
                .stream()
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList());
    }

}
