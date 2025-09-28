package com.ElihuAnalytics.ConsultorioAcupuntura.seguridad;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticacionServicio {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final UsuarioRepository usuarioRepository;

    public AutenticacionServicio(HttpServletRequest request,
                                 HttpServletResponse response,
                                 UsuarioRepository usuarioRepository) {
        this.request = request;
        this.response = response;
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<? extends Usuario> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            // Buscar la entidad REAL en la BD, usando el username
            return usuarioRepository.findByUsername(userDetails.getUsername());
        }
        return Optional.empty();
    }

    public void logout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
    }
}