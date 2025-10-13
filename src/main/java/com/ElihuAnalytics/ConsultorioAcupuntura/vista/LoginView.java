package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * LoginView
 * -----------------------------------------------
 * Vista de inicio de sesión estilizada de acuerdo
 * con el tema global y coherente con HomeView.
 *
 * - Fondo natural (home-view.css)
 * - Tarjeta central con sombra suave y esquinas redondeadas
 * - Botón de registro integrado
 * - Prevención del warning "No CopilotSession found"
 */
@Route("login")
@AnonymousAllowed
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
@CssImport("./styles/login-view.css")  // o la hoja específica de la vista
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {
        /* =====================================================
         * CONFIGURACIÓN BÁSICA DE LA VISTA
         * ===================================================== */
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // 🔑 CRÍTICO: Eliminar padding/spacing/margin
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        // 🔑 CRÍTICO: Forzar fondo transparente
        getStyle()
                .set("background", "transparent")
                .set("background-color", "transparent");

        /* =====================================================
         * TARJETA CENTRAL (similar a HomeView)
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
        titulo.addClassName("login-title"); // 🔹 estilo coherente con HomeView

        /* =====================================================
         * FORMULARIO DE LOGIN
         * ===================================================== */
        loginForm = new LoginForm();
        loginForm.setAction("login"); // 🔹 ruta por defecto para el SecurityFilter
        loginForm.setForgotPasswordButtonVisible(false);

        // ⚡ Parche para evitar el warning de Copilot en login/logout
        // Este código se ejecuta cada vez que el formulario envía un evento
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
        add(card);
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
