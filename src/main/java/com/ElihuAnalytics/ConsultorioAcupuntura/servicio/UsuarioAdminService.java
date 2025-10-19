package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Administrador;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Medico;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // <-- IMPORTA LIST
import java.util.Optional; // <-- IMPORTA OPTIONAL
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdminService(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Medico crearMedico(String username, String password,
                              String nombres, String apellidos, String celular) {
        Medico medico = new Medico(username,
                passwordEncoder.encode(password),
                celular,
                nombres,
                apellidos);
        return usuarioRepository.save(medico);
    }

    public Administrador crearAdministrador(String username, String password,
                                            String nombres, String apellidos, String celular) {
        Administrador admin = new Administrador(username,
                passwordEncoder.encode(password),
                celular,
                nombres,
                apellidos);
        return usuarioRepository.save(admin);
    }

    // --- INICIO DE LOS MÉTODOS AÑADIDOS ---

    /**
     * Lista todos los usuarios en el sistema.
     * Necesario para el Grid de administración.
     * @return Lista de todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Cambia el rol de un usuario específico.
     * Necesario para los botones de "Hacer Médico" / "Hacer Paciente".
     * @param usuarioId El ID del usuario a modificar.
     * @param nuevoRol El nuevo Rol a asignar.
     */
    @Transactional
    public void cambiarRol(Long usuarioId, Rol nuevoRol) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Evita cambiar el rol de un Administrador por seguridad
            if (usuario.getRol() != Rol.ADMINISTRADOR) {
                usuario.setRol(nuevoRol);
                usuarioRepository.save(usuario);
            }
        } else {
            // Manejar el caso de usuario no encontrado, si es necesario
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
    }
    // --- FIN DE LOS MÉTODOS AÑADIDOS ---
}