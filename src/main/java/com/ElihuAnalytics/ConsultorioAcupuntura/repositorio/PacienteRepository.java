package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findById(Long id);
}