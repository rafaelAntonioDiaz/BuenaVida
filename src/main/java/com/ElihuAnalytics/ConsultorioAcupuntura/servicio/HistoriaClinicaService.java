package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.*;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.AntecedenteRelevanteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.HistoriaClinicaRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository repo;
    private final PacienteRepository pacienteRepository;
    private final AntecedenteRelevanteRepository antecedenteRepo;

    public HistoriaClinicaService(HistoriaClinicaRepository repo,
                                  PacienteRepository pacienteRepository,
                                  AntecedenteRelevanteRepository antecedenteRepo) {
        this.repo = repo;
        this.pacienteRepository = pacienteRepository;
        this.antecedenteRepo = antecedenteRepo;
    }

    /**
     * Devuelve la historia clínica con todas las colecciones cargadas (recs, prescripciones, etc.).
     */
    @Transactional(readOnly = true)
    public Optional<HistoriaClinica> obtenerPorPacienteId(Long pacienteId) {
        return repo.findByPacienteId(pacienteId).map(this::inicializarProfundamente);
    }

    @Transactional
    public HistoriaClinica obtenerOCrearPorPacienteId(Long pacienteId) {
        return obtenerPorPacienteId(pacienteId)
                .orElseGet(() -> inicializarProfundamente(crearHistoriaClinica(pacienteId)));
    }
    private HistoriaClinica inicializarProfundamente(HistoriaClinica hc) {
        // 1) Colecciones directas
        if (hc.getRecomendaciones() != null) hc.getRecomendaciones().size();
        if (hc.getPrescripciones() != null) hc.getPrescripciones().size();

        // 2) Seguimientos y sus colecciones internas
        if (hc.getSeguimientos() != null) {
            hc.getSeguimientos().forEach(seg -> {
                if (seg.getArchivos() != null) seg.getArchivos().size();
                if (seg.getDescripcionesPorRuta() != null) seg.getDescripcionesPorRuta().size();
            });
        }

        // 3) Antecedente relevante y sus colecciones internas
        var ant = hc.getAntecedenteRelevante();
        if (ant != null) {
            if (ant.getRutasArchivos() != null) ant.getRutasArchivos().size();
            if (ant.getDescripcionesPorRuta() != null) ant.getDescripcionesPorRuta().size();
        }

        return hc;
    }


    @Transactional
    public HistoriaClinica guardar(HistoriaClinica historiaClinica) {
        if (historiaClinica.getPaciente() == null) {
            throw new IllegalArgumentException("La historia clínica debe tener un paciente asociado");
        }
        return repo.save(historiaClinica);
    }

    @Transactional
    public HistoriaClinica crearHistoriaClinica(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + pacienteId));
        HistoriaClinica nuevaHistoria = new HistoriaClinica();
        nuevaHistoria.setPaciente(paciente);
        return repo.save(nuevaHistoria);
    }

    @Transactional
    public void actualizarAntecedente(Long historiaId, String descripcion, List<String> rutasArchivos) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));

        AntecedenteRelevante ant = hc.getAntecedenteRelevante();
        if (ant == null) {
            ant = antecedenteRepo.findByHistoriaClinicaId(historiaId).orElse(null);
        }
        if (ant == null) {
            ant = new AntecedenteRelevante();
            ant.setHistoriaClinica(hc);
            hc.setAntecedenteRelevante(ant);
        }

        ant.setDescripcion(descripcion);
        ant.setRutasArchivos(rutasArchivos != null ? rutasArchivos : List.of());

        repo.save(hc);
    }

    @Transactional
    public void agregarSeguimiento(Long historiaId, String descripcion, List<String> rutasArchivos) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        SeguimientoSalud seguimiento = new SeguimientoSalud();
        seguimiento.setDescripcion(descripcion);
        seguimiento.setArchivos(rutasArchivos);
        seguimiento.setHistoriaClinica(hc);
        hc.getSeguimientos().add(seguimiento);
        repo.save(hc);
    }
    @Transactional(readOnly = true)
    public Optional<HistoriaClinica> obtenerPorPacienteIdDeHistoria(Long historiaId) {
        return repo.findById(historiaId);
    }


    // ===================== Prescripciones =====================

    @Transactional
    public HistoriaClinica agregarPrescripcion(Long historiaId, String indicaciones) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        Prescripcion pr = new Prescripcion();
        pr.setIndicaciones(indicaciones);
        pr.setHistoriaClinica(hc);
        hc.getPrescripciones().add(pr);
        return repo.save(hc);
    }

    @Transactional
    public HistoriaClinica actualizarPrescripcion(Long historiaId, Long prescripcionId, String indicaciones) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        Prescripcion pr = hc.getPrescripciones().stream()
                .filter(p -> p.getId() != null && p.getId().equals(prescripcionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Prescripción no encontrada con ID: " + prescripcionId));
        pr.setIndicaciones(indicaciones);
        return repo.save(hc);
    }

    @Transactional
    public HistoriaClinica eliminarPrescripcion(Long historiaId, Long prescripcionId) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        boolean removed = hc.getPrescripciones().removeIf(p -> p.getId() != null && p.getId().equals(prescripcionId));
        if (!removed) {
            throw new EntityNotFoundException("Prescripción no encontrada con ID: " + prescripcionId);
        }
        return repo.save(hc);
    }

    @Transactional(readOnly = true)
    public List<Prescripcion> listarPrescripciones(Long historiaId) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        // Asegurar inicialización en tx antes de ordenar
        hc.getPrescripciones().size();
        return hc.getPrescripciones().stream()
                .sorted(Comparator.comparing(Prescripcion::getFecha).reversed())
                .toList();
    }

    // ===================== Recomendaciones y Diagnóstico =====================

    @Transactional
    public HistoriaClinica agregarRecomendacion(Long historiaId, String contenido) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        Recomendacion rec = new Recomendacion();
        rec.setContenido(contenido);
        rec.setHistoriaClinica(hc);
        hc.getRecomendaciones().add(rec);
        return repo.save(hc);
    }

    @Transactional
    public HistoriaClinica actualizarDiagnosticoTradicional(Long historiaId, String diagnostico) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        hc.setDiagnosticoTradicional(diagnostico);
        return repo.save(hc);
    }

    @Transactional(readOnly = true)
    public List<Recomendacion> listarRecomendaciones(Long historiaId) {
        HistoriaClinica hc = repo.findById(historiaId)
                .orElseThrow(() -> new EntityNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        hc.getRecomendaciones().size();
        return hc.getRecomendaciones().stream()
                .sorted(Comparator.comparing(Recomendacion::getFecha).reversed())
                .toList();
    }

    // ===================== Adjuntos =====================

    public static class AdjuntoInfo {
        private final String nombre;
        private final String urlDescarga;
        public AdjuntoInfo(String nombre, String urlDescarga) {
            this.nombre = nombre;
            this.urlDescarga = urlDescarga;
        }
        public String getNombre() { return nombre; }
        public String getUrlDescarga() { return urlDescarga; }
    }

    @Transactional
    public void registrarAdjunto(Long pacienteId, String rutaRelativa, String nombreOriginal) {
        HistoriaClinica hc = obtenerPorPacienteId(pacienteId)
                .orElseGet(() -> crearHistoriaClinica(pacienteId));
        SeguimientoSalud seguimiento = new SeguimientoSalud();
        String descripcion = (nombreOriginal == null || nombreOriginal.isBlank())
                ? "Adjunto subido"
                : "Adjunto: " + nombreOriginal;
        seguimiento.setDescripcion(descripcion);
        seguimiento.setArchivos(List.of(rutaRelativa));
        seguimiento.setHistoriaClinica(hc);
        hc.getSeguimientos().add(seguimiento);
        repo.save(hc);
    }

    public List<AdjuntoInfo> listarAdjuntos(Long pacienteId) {
        HistoriaClinica hc = obtenerPorPacienteId(pacienteId)
                .orElseGet(() -> crearHistoriaClinica(pacienteId));
        List<AdjuntoInfo> res = new ArrayList<>();
        if (hc.getAntecedenteRelevante() != null && hc.getAntecedenteRelevante().getRutasArchivos() != null) {
            for (String ruta : hc.getAntecedenteRelevante().getRutasArchivos()) {
                res.add(new AdjuntoInfo(extraerNombre(ruta), construirUrl(ruta)));
            }
        }
        if (hc.getSeguimientos() != null) {
            for (SeguimientoSalud s : hc.getSeguimientos()) {
                if (s.getArchivos() != null) {
                    for (String ruta : s.getArchivos()) {
                        res.add(new AdjuntoInfo(extraerNombre(ruta), construirUrl(ruta)));
                    }
                }
            }
        }
        return res;
    }

    private static String extraerNombre(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) return "archivo";
        String base = rutaRelativa.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        return (idx >= 0 && idx + 1 < base.length()) ? base.substring(idx + 1) : base;
    }

    private static String construirUrl(String rutaRelativa) {
        String fileName = extraerNombre(rutaRelativa);
        return "/files/" + fileName;
    }
}
