package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    // Busca el último código no verificado para un email
    Optional<CodigoVerificacion> findTopByEmailAndVerificadoFalseOrderByGeneradoElDesc(String email);

    // Verifica si un código específico existe y no está usado para un email
    Optional<CodigoVerificacion> findByEmailAndCodigoAndVerificadoFalse(String email, String codigo);
}

