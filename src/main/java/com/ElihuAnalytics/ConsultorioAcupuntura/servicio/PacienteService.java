package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repo;

    public PacienteService(PacienteRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void actualizarRutaFotoPerfil(Long pacienteId, String rutaWeb) {
        Paciente p = repo.findById(pacienteId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado: " + pacienteId));
        p.setRutaFotoPerfil(rutaWeb);
        repo.save(p);
    }

    public List<Paciente> listarTodos() {
        return repo.findAll();

    }
    @Transactional(readOnly = true)
    public Paciente buscarPorUsuarioId(Long usuarioId) {
        return repo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un paciente con ID de usuario: " + usuarioId));
    }
}