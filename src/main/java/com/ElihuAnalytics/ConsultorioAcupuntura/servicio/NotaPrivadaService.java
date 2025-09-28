package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotaPrivada;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.NotaPrivadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio para Notas Privadas del médico.
 * Encapsula la persistencia y el orden de presentación (descendente por fecha).
 */
@Service
@Transactional
public class NotaPrivadaService {

    private final NotaPrivadaRepository repo;

    public NotaPrivadaService(NotaPrivadaRepository repo) {
        this.repo = repo;
    }

    // Crear una nueva nota (se fecha automáticamente con @CreationTimestamp)
    public NotaPrivada crear(HistoriaClinica hc, String texto) {
        NotaPrivada n = new NotaPrivada(hc, texto);
        return repo.save(n);
    }

    // Listar por historia, más recientes primero
    @Transactional(readOnly = true)
    public List<NotaPrivada> listarDesc(Long historiaClinicaId) {
        return repo.findByHistoriaClinica_IdOrderByFechaHoraDesc(historiaClinicaId);
    }

    // Actualizar el texto de una nota existente
    public NotaPrivada actualizar(Long id, String nuevoTexto) {
        NotaPrivada n = repo.findById(id).orElseThrow();
        n.setTexto(nuevoTexto);
        return repo.save(n);
    }

    // Eliminar una nota (opcional)
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}