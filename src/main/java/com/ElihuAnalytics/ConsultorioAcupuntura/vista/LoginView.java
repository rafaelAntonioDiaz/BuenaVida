package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol; // <-- IMPORTAR ROL
import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio; // <-- IMPORTAR SERVICIO
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
// import com.vaadin.flow.component.html.Paragraph; // No se usa
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired; // <-- IMPORTAR AUTOWIRED

/**
 * LoginView
 * Corregido para redirigir usuarios ya autenticados.
 */
@Route("login")
@AnonymousAllowed
@CssImport(value = "./styles/global-theme.css", themeFor = "vaadin-login-form")
@CssImport(value = "./styles/vaadin-components.css", themeFor = "vaadin-login-form")
@CssImport(value = "./styles/vaadin-overrides.css", themeFor = "vaadin-login-form")
@CssImport("./styles/login-view.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm(); // Hecho final
    private final AutenticacionServicio authService; // <-- INYECTAR SERVICIO

    @Autowired // <-- AÑADIR AUTOWIRED AL CONSTRUCTOR
    public LoginView(AutenticacionServicio authService) {
        this.authService = authService; // <-- ASIGNAR SERVICIO

        addClassName("login-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        getStyle().set("background", "transparent").set("background-color", "transparent");

        Div header = crearHeader();

        VerticalLayout contenidoPrincipal = new VerticalLayout();
        contenidoPrincipal.addClassName("login-view__content");
        contenidoPrincipal.setSizeFull();
        contenidoPrincipal.setPadding(false);
        contenidoPrincipal.setSpacing(false);
        contenidoPrincipal.setAlignItems(Alignment.CENTER);
        contenidoPrincipal.setJustifyContentMode(JustifyContentMode.CENTER);
        contenidoPrincipal.getStyle().set("background", "transparent").set("background-color", "transparent");

        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);

        H1 titulo = new H1("Por favor inicia sesión");
        titulo.addClassName("login-title"); // Asegúrate que el CSS haga este título oscuro

        configurarLoginForm(); // Mover configuración a método

        Button registroBtn = new Button("¿No tienes cuenta? Regístrate",
                e -> UI.getCurrent().navigate(""));
        registroBtn.addClassName("registro-btn");

        card.add(titulo, loginForm, registroBtn);
        contenidoPrincipal.add(card);
        add(header, contenidoPrincipal);
    }

    private void configurarLoginForm() {
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setUsername("Correo electrónico");
        i18nForm.setPassword("Contraseña");
        i18nForm.setSubmit("Iniciar sesión");
        i18nForm.setTitle(""); // Título interno vacío
        i18n.setForm(i18nForm); // Asignar el form modificado
        // Mensajes de error (opcional pero recomendado)
        LoginI18n.ErrorMessage i18nError = i18n.getErrorMessage();
        i18nError.setTitle("Error de autenticación");
        i18nError.setMessage("Usuario o contraseña incorrectos. Por favor, verifica tus datos.");
        i18n.setErrorMessage(i18nError);

        loginForm.setI18n(i18n);

        // Ya no es necesario forzar tema 'primary' con JS si el CSS global lo maneja
    }

    private Div crearHeader() {
        Div header = new Div();
        header.addClassName("login-header");
        Image logo = new Image("/images/logo-rafael-diaz-sarmiento.svg", "Logo Consultorio"); // Usar logo grueso
        logo.addClassName("login-header__logo");
        Div textoContainer = new Div();
        textoContainer.addClassName("login-header__text");
        H2 tituloHeader = new H2("Buena Vida Medicina Ancestral");
        tituloHeader.addClassName("login-header__title"); // Asegúrate que el CSS haga este título oscuro
        textoContainer.add(tituloHeader);
        header.add(logo, textoContainer);
        return header;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // --- INICIO DE LA CORRECCIÓN ---
        // 1. Verificar si el usuario ya está autenticado
        if (authService.getUsuarioAutenticado().isPresent()) {
            // 2. Si está autenticado, redirigir según su rol
            Rol rolUsuario = authService.getUsuarioAutenticado().get().getRol();
            String targetUrl = "/"; // Destino por defecto (Home) si algo falla

            if (rolUsuario == Rol.ADMINISTRADOR || rolUsuario == Rol.MEDICO) {
                targetUrl = "/admin"; // Médicos y Admin van al panel unificado
            } else if (rolUsuario == Rol.PACIENTE) {
                targetUrl = "/paciente";
            }

            // event.rerouteTo() es mejor que navigate() en BeforeEnter
            event.rerouteTo(targetUrl);
        } else {
            // 3. Si NO está autenticado, verificar si hay error de login (lógica original)
            if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
                loginForm.setError(true);
            }
        }
        // --- FIN DE LA CORRECCIÓN ---
    }
}