package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * LoginView
 * -----------------------------------------------
 * Vista de inicio de sesión con header de identidad del consultorio.
 * Diseño coherente con el sistema global de la aplicación.
 *
 * Características:
 * - Header fijo con logo y nombre del consultorio
 * - Fondo natural coherente con el tema
 * - Tarjeta central con sombra suave y esquinas redondeadas
 * - Botón de registro integrado
 * - Prevención del warning "No CopilotSession found"
 */
@Route("login")
@AnonymousAllowed
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
@CssImport("./styles/login-view.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {
        /* =====================================================
         * CONFIGURACIÓN BÁSICA DE LA VISTA
         * ===================================================== */
        addClassName("login-view");
        setSizeFull();

        // 🔒 CRÍTICO: Eliminar padding/spacing/margin del contenedor raíz
        setPadding(false);
        setSpacing(false);
        setMargin(false);

        // 🔒 CRÍTICO: Forzar fondo transparente en el contenedor raíz
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

        // Forzar transparencia en el contenedor principal
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

        /* =====================================================
         * TÍTULO PRINCIPAL
         * ===================================================== */
        H1 titulo = new H1("Por favor inicia sesión");
        titulo.addClassName("login-title");

        /* =====================================================
         * FORMULARIO DE LOGIN
         * ===================================================== */
        loginForm = new LoginForm();
        loginForm.setAction("login"); // 📍 ruta por defecto para el SecurityFilter
        loginForm.setForgotPasswordButtonVisible(false);

        // ⚡ Parche para evitar el warning de Copilot en login/logout
        loginForm.addLoginListener(e -> {
            // Desconecta cualquier canal WebSocket de debug del DevServer
            UI.getCurrent().getPage().executeJs(
                    "if (window.Vaadin && Vaadin.push) { Vaadin.push.disconnect(); }"
            );
        });

        /* =====================================================
         * BOTÓN DE REGISTRO
         * ===================================================== */
        Button registroBtn = new Button("¿No tienes cuenta? Regístrate",
                e -> UI.getCurrent().navigate("registro"));
        registroBtn.addClassName("registro-btn");

        /* =====================================================
         * ENSAMBLA LA CARD
         * ===================================================== */
        card.add(titulo, loginForm, registroBtn);
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

        // Subtítulo
        Paragraph subtitulo = new Paragraph("Rafael Antonio Díaz Sarmiento");
        subtitulo.addClassName("login-header__subtitle");

        // Ensamblar texto
        textoContainer.add(titulo, subtitulo);

        // Ensamblar header
        header.add(logo, textoContainer);

        return header;
    }

    /* =====================================================
     * GESTIÓN DE PARÁMETROS (?error)
     * ===================================================== */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Si llega con ?error desde Spring Security, mostrar error visual
        if (event.getLocation().getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}