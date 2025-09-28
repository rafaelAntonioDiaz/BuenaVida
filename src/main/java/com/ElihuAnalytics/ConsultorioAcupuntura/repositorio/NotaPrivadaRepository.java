// java
package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotaPrivada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a datos de Notas Privadas.
 */
public interface NotaPrivadaRepository extends JpaRepository<NotaPrivada, Long> {

    // Listar por historia clínica, más recientes primero
    List<NotaPrivada> findByHistoriaClinica_IdOrderByFechaHoraDesc(Long historiaClinicaId);
}