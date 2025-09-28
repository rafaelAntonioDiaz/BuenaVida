package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Medico;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.UsuarioAdminService;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAdminService usuarioAdminService;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioAdminService usuarioAdminService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAdminService = usuarioAdminService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        final String usernameMedico = "ra";
        final String passPlano = "contraseña"; // en UsuarioAdminService se codifica
        final String nombres = "Rafael Antonio";
        final String apellidos = "Díaz Sarmiento";
        final String celular = "3132203002";

        usuarioRepository.findByUsername(usernameMedico).ifPresentOrElse(u -> {
            // Ya existe el username: ¿es realmente un Medico?
            if (!(u instanceof Medico) || u.getRol() != Rol.MEDICO) {
                // Transformar a Medico para garantizar dtype = 'Medico'
                usuarioRepository.delete(u);
                usuarioRepository.flush(); // libera el username (único) antes de re-crear
                usuarioAdminService.crearMedico(usernameMedico, passPlano, nombres, apellidos, celular);
                System.out.println("[DataInitializer] Usuario existente convertido a Medico: " + usernameMedico);
            } else {
                System.out.println("[DataInitializer] Medico ya presente: " + usernameMedico);
            }
        }, () -> {
            usuarioAdminService.crearMedico(usernameMedico, passPlano, nombres, apellidos, celular);
            System.out.println("[DataInitializer] Medico creado: " + usernameMedico);
        });
    }
}