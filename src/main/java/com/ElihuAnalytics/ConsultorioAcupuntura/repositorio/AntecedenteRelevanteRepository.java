package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.AntecedenteRelevante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AntecedenteRelevanteRepository extends JpaRepository<AntecedenteRelevante, Long> {
    Optional<AntecedenteRelevante> findByHistoriaClinicaId(Long historiaClinicaId);
}