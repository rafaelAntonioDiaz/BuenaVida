package com.ElihuAnalytics.ConsultorioAcupuntura.seguridad;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio para cargar usuarios desde la base de datos.
 * Spring Security usa esta clase durante el login.
 */
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepository usuarioRepositorio;
    private final PacienteRepository pacienteRepositorio;

    public UsuarioDetallesServicio(UsuarioRepository usuarioRepositorio,
                                   PacienteRepository pacienteRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Si es paciente, devolver la entidad Paciente en lugar de Usuario
        if (usuario.getRol() == Rol.PACIENTE) {
            return pacienteRepositorio.findById(usuario.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Paciente no encontrado: " + username));
        }

        return usuario;
    }
}
