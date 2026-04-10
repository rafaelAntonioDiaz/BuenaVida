package com.ElihuAnalytics.ConsultorioAcupuntura.seguridad;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.UsuarioDetallesServicio;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

/**
 * Configuración central de Seguridad para la aplicación del Consultorio.
 * Integra Spring Security 6 con Vaadin Flow.
 * * Reglas principales:
 * 1. Acceso público a rutas estáticas y de registro.
 * 2. Redirección basada en roles (Admin, Médico, Paciente).
 * 3. Acceso restringido a la consola de base de datos H2 (Solo Médico/Admin).
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig extends VaadinWebSecurity {

    private final UsuarioDetallesServicio usuarioDetallesServicio;

    public SeguridadConfig(UsuarioDetallesServicio usuarioDetallesServicio) {
        this.usuarioDetallesServicio = usuarioDetallesServicio;
    }

    /**
     * Define a dónde debe ir cada usuario después de hacer login exitosamente.
     * Evalúa los roles (Authorities) y redirige al panel correspondiente.
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();

            String redirectUrl = "/"; // Ruta por defecto (Home)

            // Lógica de redirección según el rol del usuario
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
                redirectUrl = "/admin";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"))) {
                redirectUrl = "/medico";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"))) {
                redirectUrl = "/paciente";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    /**
     * Manejador personalizado para el cierre de sesión (Logout).
     * Asegura que el contexto de Spring Security se limpie completamente
     * antes de que Vaadin intente recargar la página, evitando bucles de redirección.
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                // Limpieza profunda del contexto de seguridad
                new SecurityContextLogoutHandler().logout(request, response, authentication);
            }
            // Redirigir a la página de inicio pública tras cerrar sesión
            response.sendRedirect("/");
        };
    }

    /**
     * Configuración del filtro de seguridad HTTP.
     * Define qué rutas son públicas, cuáles requieren login y las excepciones de seguridad.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Evita que Spring recuerde la URL protegida a la que el usuario intentó acceder antes de loguearse.
                // Vaadin maneja esto mejor por su cuenta.
                .requestCache(c -> c.requestCache(new NullRequestCache()))

                // Prevención de ataques de Fijación de Sesión (Session Fixation).
                // Genera un nuevo ID de sesión al loguearse.
                .sessionManagement(sm -> sm.sessionFixation(sf -> sf.migrateSession()))

                // --- CONFIGURACIÓN CSRF (Cross-Site Request Forgery) ---
                .csrf(csrf -> csrf
                        // Guardar el token en una cookie accesible (necesario para Vaadin/JS)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // EXCEPCIONES CSRF:
                        // 1. El formulario de login de Vaadin no envía token CSRF por defecto.
                        .ignoringRequestMatchers(new RegexRequestMatcher("^/login$", "POST"))
                        // 2. La consola H2 es una herramienta antigua que no soporta tokens CSRF.
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // --- REGLAS DE AUTORIZACIÓN DE RUTAS ---
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas Públicas (Permitidas para todos sin login)
                        .requestMatchers(
                                "/", "/registro", "/verificar", "/login",
                                "/images/**", "/icons/**", "/favicon.ico", // Recursos visuales
                                "/VAADIN/**", "/frontend/**", "/webjars/**", "/line-awesome/**", // Framework
                                "/styles/**", "/manifest.webmanifest", "/sw.js", "/offline.html", // PWA
                                "/files/**", "/pacientes-Uploads/**", // Archivos adjuntos y fotos
                                "/sitemap.xml"
                        ).permitAll()

                        // 2. Zona VIP (Base de datos): Solo para personal médico y administrativo.
                        // NOTA DE MANTENIMIENTO: No hacer pública esta ruta jamás en producción.
                        .requestMatchers("/h2-console/**").hasAnyRole("ADMINISTRADOR", "MEDICO")

                        // 3. Paneles Privados según rol
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMINISTRADOR", "MEDICO")
                        .requestMatchers("/medico", "/medico/**").hasAnyRole("MEDICO", "ADMINISTRADOR")
                        .requestMatchers("/paciente", "/paciente/**").hasRole("PACIENTE")
                )

                // --- CONFIGURACIÓN DE LOGOUT ---
                .logout(logout -> logout
                        // Permitir desloguearse usando el método GET (URL directa) y POST (Botones)
                        .logoutRequestMatcher(new RegexRequestMatcher("^/logout$", "GET"))
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .clearAuthentication(true) // Borrar credenciales de memoria
                        .invalidateHttpSession(true) // Destruir la sesión en el servidor
                        .deleteCookies("JSESSIONID") // Borrar la cookie del navegador
                )

                // --- CONFIGURACIÓN DE CABECERAS (HEADERS) ---
                .headers(headers -> headers
                        // Permite renderizar iframes si provienen del mismo dominio.
                        // CRÍTICO: Necesario para que la consola H2 funcione, ya que usa <frame> para su UI.
                        .frameOptions(frame -> frame.sameOrigin())
                        .cacheControl(org.springframework.security.config.Customizer.withDefaults()));

        // Aplicar la configuración base de VaadinWebSecurity
        super.configure(http);

        // Definir la vista de login de Vaadin y aplicar el manejador de roles
        setLoginView(http, "login", "/");
        http.formLogin(form -> form.successHandler(roleBasedSuccessHandler()));
    }

    /**
     * Define el algoritmo de encriptación para las contraseñas.
     * BCrypt es el estándar actual de la industria por su seguridad y control de "salting".
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}