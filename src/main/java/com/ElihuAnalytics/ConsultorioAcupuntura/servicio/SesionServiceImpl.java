package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.SesionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class SesionServiceImpl implements SesionService {

    private final SesionRepository sesionRepository;

    public SesionServiceImpl(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    public List<Sesion> obtenerSesionesPorPacienteYMes(Long pacienteId, YearMonth yearMonth) {
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    @Override
    public List<Sesion> buscarPendientesEntre(LocalDateTime inicio, LocalDateTime fin) {
        return sesionRepository.findByFechaBetweenAndEstadoIn(inicio, fin, List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA));
    }

    @Override
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

    @Override
    public void cancelarSesion(Long sesionId) {
        sesionRepository.findById(sesionId).ifPresent(sesion -> {
            sesion.setEstado(Sesion.EstadoSesion.CANCELADA);
            sesionRepository.save(sesion);
        });
    }

    @Override
    public void guardarSesion(Sesion sesion) {
        sesionRepository.save(sesion);
    }

    @Override
    public List<Sesion> buscarPendientesAntes(LocalDateTime fecha) {
        return sesionRepository.findByFechaBeforeAndEstadoIn(fecha, List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA));
    }

    @Override
    public boolean estaDisponible(LocalDateTime fecha, Duration duracion) {
        LocalDateTime fin = fecha.plus(duracion);
        List<Sesion> sesiones = sesionRepository.findByFechaBetweenAndEstadoIn(
                fecha.minusMinutes(30), // Margen de 30 minutos antes
                fin.plusMinutes(30),    // Margen de 30 minutos después
                List.of(Sesion.EstadoSesion.PROGRAMADA, Sesion.EstadoSesion.CONFIRMADA)
        );
        return sesiones.isEmpty();
    }

    @Override
    public List<Sesion> obtenerSesionesPorPacienteYDia(Long pacienteId, LocalDate dia) {
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(23, 59, 59);
        return sesionRepository.findByPacienteIdAndFechaBetween(pacienteId, inicio, fin);
    }

    @Override
    public Optional<Sesion> reprogramarSesion(Long sesionId, LocalDateTime nuevaFecha, Duration duracion) {
        Optional<Sesion> sesionOpt = sesionRepository.findById(sesionId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            if (sesion.getEstado() == Sesion.EstadoSesion.PROGRAMADA || sesion.getEstado() == Sesion.EstadoSesion.CONFIRMADA) {
                if (estaDisponible(nuevaFecha, duracion)) {
                    sesion.setFecha(nuevaFecha);
                    sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA); // Volver a PROGRAMADA al reprogramar
                    sesion.setRecordatorioEnviado(false); // Resetear bandera de recordatorio
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