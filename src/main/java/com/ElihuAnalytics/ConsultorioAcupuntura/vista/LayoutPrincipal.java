package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Layout principal de la aplicación.
 * Contiene la barra superior (topbar) con:
 *  - Logo / título.
 *  - Saludo al usuario autenticado.
 *  - Botones de acción (notificación demo y cerrar sesión).
 *
 * En modo producción (app.demo.enabled = false), se oculta el botón de notificaciones.
 */
@UIScope
@Component
@PermitAll
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
@CssImport("./styles/app-layout.css") // o la hoja específica de la vista
public class LayoutPrincipal extends AppLayout {

    @Autowired
    private AutenticacionServicio auth;

    // Variable que controla el modo demo (viene de application.properties)
    @Value("${app.demo.enabled:false}")
    private boolean demoEnabled;

    @PostConstruct
    public void init() {
        crearEncabezado();
    }

    /**
     * Crea el encabezado principal de la aplicación (topbar).
     * Se construye de forma flexible para adaptarse a diferentes tamaños de pantalla.
     */
    private void crearEncabezado() {
        // === Logo / Título principal ===
        H1 logo = new H1("Buena Vida Medicina Ancestral");
        logo.getStyle()
                .set("margin", "0")
                .set("flex-shrink", "0") // evita que se encoja
                .set("min-width", "fit-content");

        // === Saludo al usuario ===
        Span nombreUsuario = new Span();
        auth.getUsuarioAutenticado().ifPresent(usuario -> {
            nombreUsuario.setText("Hola " + usuario.getNombres() + "!");
        });
        nombreUsuario.getStyle()
                .set("flex-grow", "1") // ocupa el espacio central
                .set("text-align", "center")
                .set("min-width", "150px");

        // === Contenedor de botones (lado derecho) ===
        HorizontalLayout botonesDerechos = new HorizontalLayout();
        botonesDerechos.setSpacing(true);
        botonesDerechos.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        botonesDerechos.getStyle()
                .set("flex-shrink", "0")
                .set("gap", "0.5rem");

        // --- Botón de notificaciones (solo visible en modo demo) ---
        if (demoEnabled) {
            Button demoBtn = new Button(VaadinIcon.BELL.create());
            demoBtn.setTooltipText("Demo Notificaciones");
            demoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            demoBtn.addClickListener(e -> UI.getCurrent().navigate("demo-notificaciones"));
            demoBtn.getStyle()
                    .set("color", "var(--lumo-warning-color)")
                    .set("min-width", "40px");
            botonesDerechos.add(demoBtn);
        }

        // --- Botón de cerrar sesión ---
        Button logout = new Button("Cerrar sesión", e -> {
            auth.logout();
            UI.getCurrent().getPage().setLocation("/logout");
        });
        logout.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        logout.getStyle()
                .set("white-space", "nowrap") // evita que se rompa la línea
                .set("min-width", "fit-content");

        botonesDerechos.add(logout);

        // === Barra principal (logo + saludo + botones) ===
        HorizontalLayout barra = new HorizontalLayout(logo, nombreUsuario, botonesDerechos);
        barra.addClassName("app-topbar");
        barra.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        barra.setWidthFull();
        barra.setSpacing(true);
        barra.getStyle()
                .set("box-sizing", "border-box")
                .set("flex-wrap", "wrap"); // previene desbordes en pantallas pequeñas

        // Agrega la barra al área superior del layout
        addToNavbar(barra);
    }
}
