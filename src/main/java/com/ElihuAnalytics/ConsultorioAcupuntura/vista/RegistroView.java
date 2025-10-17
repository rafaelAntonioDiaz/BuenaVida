package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

/**
 * RegistroView - Vista de registro de nuevos pacientes.
 * Diseño coherente con LoginView usando el mismo sistema visual con header de identidad.
 */
@Route("registro")
@PageTitle("Registro de Usuario")
@AnonymousAllowed
@CssImport("./styles/global-theme.css")
@CssImport("./styles/login-view.css")
public class RegistroView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(RegistroView.class);

    private final UsuarioRepository usuarioRepositorio;
    private final PacienteRepository pacienteRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email:clubwebapp2025@gmail.com}")
    private String fromEmail;

    public RegistroView(UsuarioRepository usuarioRepositorio,
                        PacienteRepository pacienteRepositorio,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.passwordEncoder = passwordEncoder;

        /* =====================================================
         * CONFIGURACIÓN BÁSICA DE LA VISTA
         * ===================================================== */
        addClassName("login-view");
        setSizeFull();

        // 🔒 CRÍTICO: Eliminar padding/spacing/margin del contenedor raíz
        setPadding(false);
        setSpacing(false);
        setMargin(false);

        // 🔒 CRÍTICO: Forzar fondo transparente
        getStyle()
                .set("background", "transparent")
                .set("background-color", "transparent");

        /* =====================================================
         * HEADER DE IDENTIDAD DEL CONSULTORIO
         * ===================================================== */
        Div header = crearHeader();

        /* =====================================================
         * CONTENEDOR PRINCIPAL (con espaciado para header fijo)
         * ===================================================== */
        VerticalLayout contenidoPrincipal = new VerticalLayout();
        contenidoPrincipal.addClassName("login-view__content");
        contenidoPrincipal.setSizeFull();
        contenidoPrincipal.setPadding(false);
        contenidoPrincipal.setSpacing(false);
        contenidoPrincipal.setAlignItems(Alignment.CENTER);
        contenidoPrincipal.setJustifyContentMode(JustifyContentMode.CENTER);

        // Forzar transparencia
        contenidoPrincipal.getStyle()
                .set("background", "transparent")
                .set("background-color", "transparent");

        /* =====================================================
         * TARJETA CENTRAL
         * ===================================================== */
        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.setWidth("100%");
        card.setMaxWidth("450px"); // Un poco más ancho para los campos extra

        /* =====================================================
         * TÍTULO Y DESCRIPCIÓN
         * ===================================================== */
        H1 titulo = new H1("Crea tu cuenta");
        titulo.getStyle()
                .set("color", "var(--color-primary-dark)")
                .set("margin", "0")
                .set("margin-bottom", "0.5rem");

        Paragraph subtitulo = new Paragraph("Completa tus datos para registrarte");
        subtitulo.getStyle()
                .set("color", "var(--color-text-medium)")
                .set("margin", "0")
                .set("margin-bottom", "1rem")
                .set("text-align", "center");

        /* =====================================================
         * CAMPOS DEL FORMULARIO
         * ===================================================== */
        TextField nombresField = new TextField("Nombres");
        nombresField.setWidthFull();
        nombresField.setRequired(true);

        TextField apellidosField = new TextField("Apellidos");
        apellidosField.setWidthFull();
        apellidosField.setRequired(true);

        TextField celularField = new TextField("Celular");
        celularField.setWidthFull();
        celularField.setRequired(true);
        celularField.setPlaceholder("3001234567");

        EmailField emailField = new EmailField("Correo electrónico");
        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setPlaceholder("usuario@dominio.com");

        PasswordField passwordField = new PasswordField("Contraseña");
        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setMinLength(6);

        TextField codigoVerificacionField = new TextField("Código de verificación");
        codigoVerificacionField.setWidthFull();
        codigoVerificacionField.setPlaceholder("Ingresa el código recibido");

        /* =====================================================
         * BOTONES
         * ===================================================== */
        Button enviarCodigoBtn = new Button("Enviar código");
        enviarCodigoBtn.setWidthFull();
        enviarCodigoBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        enviarCodigoBtn.getStyle()
                .set("margin-top", "0.5rem");

        Button registrarBtn = new Button("Registrar");
        registrarBtn.setWidthFull();
        registrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrarBtn.getStyle()
                .set("margin-top", "0.5rem");

        Button volverBtn = new Button("¿Ya tienes cuenta? Inicia sesión",
                e -> UI.getCurrent().navigate("login"));
        volverBtn.setWidthFull();
        volverBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        volverBtn.addClassName("registro-btn");

        /* =====================================================
         * LÓGICA DE VERIFICACIÓN
         * ===================================================== */
        final String[] codigoGenerado = new String[1];

        enviarCodigoBtn.addClickListener(e -> {
            String email = emailField.getValue();
            if (email == null || email.isBlank()) {
                Notification.show("Por favor, ingresa un correo válido.",
                        4000, Notification.Position.MIDDLE);
                return;
            }

            // Generar código aleatorio de 6 dígitos
            codigoGenerado[0] = String.valueOf(new Random().nextInt(900000) + 100000);

            try {
                SendGrid sg = new SendGrid(sendgridApiKey);
                Email from = new Email(fromEmail);
                Email to = new Email(email);
                Content content = new Content("text/plain",
                        "Tu código de verificación es: " + codigoGenerado[0]);
                Mail mail = new Mail(from, "Código de verificación - Consultorio Acupuntura", to, content);

                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);

                if (response.getStatusCode() == 202) {
                    Notification.show("Código enviado a " + email,
                            3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Error al enviar el correo. Código: " + response.getStatusCode(),
                            5000, Notification.Position.MIDDLE);
                }
            } catch (Exception ex) {
                logger.error("Error al enviar correo: {}", ex.getMessage());
                Notification.show("Error al enviar correo: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        });

        registrarBtn.addClickListener(e -> {
            try {
                // Validar código
                if (codigoGenerado[0] == null || !codigoVerificacionField.getValue().equals(codigoGenerado[0])) {
                    Notification.show("Código incorrecto o no enviado.",
                            4000, Notification.Position.MIDDLE);
                    return;
                }

                String email = emailField.getValue();
                if (usuarioRepositorio.existsByUsername(email)) {
                    Notification.show("Ya existe una cuenta con este correo.",
                            4000, Notification.Position.MIDDLE);
                    return;
                }

                // Crear paciente
                Paciente p = new Paciente();
                p.setNombres(nombresField.getValue());
                p.setApellidos(apellidosField.getValue());
                p.setCelular(celularField.getValue());
                p.setUsername(email);
                p.setPassword(passwordEncoder.encode(passwordField.getValue()));
                p.setRol(Rol.PACIENTE);
                p.setActivo(true);
                pacienteRepositorio.save(p);

                Notification.show("¡Registro exitoso! Bienvenido/a " + p.getNombres(),
                        4000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("login");

            } catch (Exception ex) {
                logger.error("Error en registro: {}", ex.getMessage());
                Notification.show("Error en el registro: " + ex.getMessage(),
                        4000, Notification.Position.MIDDLE);
            }
        });

        /* =====================================================
         * ENSAMBLAR LA CARD
         * ===================================================== */
        card.add(
                titulo,
                subtitulo,
                nombresField,
                apellidosField,
                celularField,
                emailField,
                passwordField,
                enviarCodigoBtn,
                codigoVerificacionField,
                registrarBtn,
                volverBtn
        );

        contenidoPrincipal.add(card);

        /* =====================================================
         * ENSAMBLA LA VISTA COMPLETA
         * ===================================================== */
        add(header, contenidoPrincipal);
    }

    /**
     * Crea el header de identidad del consultorio con logo y nombre.
     *
     * @return Div contenedor del header
     */
    private Div crearHeader() {
        Div header = new Div();
        header.addClassName("login-header");

        // Logo del consultorio
        Image logo = new Image("/images/logo-rafael-diaz-sarmiento.svg", "Logo Consultorio");
        logo.addClassName("login-header__logo");

        // Contenedor de texto
        Div textoContainer = new Div();
        textoContainer.addClassName("login-header__text");

        // Título principal
        H2 titulo = new H2("Buena Vida Medicina Ancestral");
        titulo.addClassName("login-header__title");


        // Ensamblar texto
        textoContainer.add(titulo);

        // Ensamblar header
        header.add(logo, textoContainer);

        return header;
    }
}