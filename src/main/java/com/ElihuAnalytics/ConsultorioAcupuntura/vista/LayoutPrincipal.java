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
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@UIScope
@Component
@PermitAll
@CssImport("./styles/app-layout.css")
public class LayoutPrincipal extends AppLayout {

    @Autowired
    private AutenticacionServicio auth;

    @Value("${app.demo.enabled:true}")
    private boolean demoEnabled;

    @PostConstruct
    public void init() {
        crearEncabezado();
    }

    private void crearEncabezado() {
        H1 logo = new H1("Buena Vida con Medicina Ancestral");
        logo.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("margin", "0");

        Span nombreUsuario = new Span();
        auth.getUsuarioAutenticado().ifPresent(usuario -> {
            String nombre = usuario.getNombres();
            nombreUsuario.setText("Bienvenido, " + nombre);
        });

        // Layout para botones del lado derecho
        HorizontalLayout botonesDerechos = new HorizontalLayout();
        botonesDerechos.setSpacing(true);
        botonesDerechos.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Botón demo notificaciones (solo si está habilitado)
        if (demoEnabled) {
            Button demoBtn = new Button(VaadinIcon.BELL.create());
            demoBtn.setTooltipText("Demo Notificaciones");
            demoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            demoBtn.addClickListener(e -> UI.getCurrent().navigate("demo-notificaciones"));

            // Estilo del botón demo
            demoBtn.getStyle()
                    .set("color", "var(--lumo-warning-color)")
                    .set("margin-right", "var(--lumo-space-s)");

            botonesDerechos.add(demoBtn);
        }

        Button logout = new Button("Cerrar sesión", e -> {
            com.vaadin.flow.component.UI.getCurrent().getPage().setLocation("/logout");
            auth.logout();
        });
        logout.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        botonesDerechos.add(logout);

        HorizontalLayout barra = new HorizontalLayout(logo, nombreUsuario, botonesDerechos);
        barra.addClassName("app-topbar");
        barra.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        barra.setWidthFull();
        barra.setSpacing(true);
        barra.getStyle().set("padding", "var(--lumo-space-m)");
        barra.expand(nombreUsuario); // Expandir el área del usuario para centrar

        addToNavbar(barra);
    }
}