package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<? extends Usuario> findByUsername(String username);
    boolean existsByUsername(String username);

}
