package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Administrador;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Medico;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
