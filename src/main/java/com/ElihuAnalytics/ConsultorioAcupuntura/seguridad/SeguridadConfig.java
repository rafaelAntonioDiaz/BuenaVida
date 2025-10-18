package com.ElihuAnalytics.ConsultorioAcupuntura.seguridad;

// Importamos la clase que faltaba en tu código original
import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.UsuarioDetallesServicio;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

/**
 * Claves:
 * - NO usar anyRequest() aquí (VaadinWebSecurity lo añade según corresponde).
 * - Ignorar CSRF para POST /login (el LoginForm no envía token CSRF).
 * - Tras login, redirigir SIEMPRE a "/" y ahí la UI decide por rol.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig extends VaadinWebSecurity {

    private final UsuarioDetallesServicio usuarioDetallesServicio;

    public SeguridadConfig(UsuarioDetallesServicio usuarioDetallesServicio) {
        this.usuarioDetallesServicio = usuarioDetallesServicio;
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();

            String redirectUrl = "/"; // fallback
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // No recordar la última URL protegida
                .requestCache(c -> c.requestCache(new NullRequestCache()))
                // Migrar la sesión al autenticarse
                .sessionManagement(sm -> sm.sessionFixation(sf -> sf.migrateSession()))
                // CSRF: token en cookie para logout, pero ignorado para POST /login
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(new RegexRequestMatcher("^/login$", "POST"))
                )
                // Autorización por rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/registro", "/verificar", "/login",
                                // Recursos estáticos y del framework
                                "/images/**", "/icons/**", "/favicon.ico",
                                "/VAADIN/**", "/frontend/**", "/webjars/**", "/line-awesome/**",
                                "/styles/**", "/manifest.webmanifest", "/sw.js", "/offline.html",
                                "/files/**",
                                "/sitemap.xml"
                        ).permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/medico", "/medico/**").hasAnyRole("MEDICO", "ADMINISTRADOR")
                        .requestMatchers("/paciente", "/paciente/**").hasRole("PACIENTE")
                )
                .logout(logout -> logout
                        // Permitir logout también por GET
                        .logoutRequestMatcher(new RegexRequestMatcher("^/logout$", "GET"))
                        // Mantener compatibilidad con POST /logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                // Evitar caché de páginas protegidas
                .headers(headers -> headers.cacheControl(org.springframework.security.config.Customizer.withDefaults()));

        // Integración Vaadin + página de login y success handler
        super.configure(http);

        setLoginView(http, "login");
        http.formLogin(form -> form.successHandler(roleBasedSuccessHandler()));
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}