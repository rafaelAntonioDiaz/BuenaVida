// java
package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@Route(value = "medico", layout = LayoutPrincipal.class)
@PageTitle("Médico")
@RolesAllowed({"ADMINISTRADOR", "MEDICO"})
@CssImport("styles/medico-admin.css")
public class MedicoAdministradorView extends VerticalLayout {

    private final AutenticacionServicio auth;

    public MedicoAdministradorView(AutenticacionServicio auth) {
        this.auth = auth;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        addClassName("medico-admin-view");

        String saludo = auth.getUsuarioAutenticado()
                .map(u -> "Hola, " + Optional.ofNullable(u.getNombres()).orElse("Doctor") + " 👋")
                .orElse("Hola, Doctor 👋");

        H2 titulo = new H2(saludo);
        Paragraph subtitulo = new Paragraph("Que vamos a hacer?");

        Button btnCitas = new Button("Gestionar Citas", e -> UI.getCurrent().navigate("medico/citas"));
        btnCitas.getElement().getThemeList().add("primary");
        btnCitas.setWidth("320px");

        Button btnHistorias = new Button("Actualizar historias clínicas", e -> UI.getCurrent().navigate("medico/tratamiento"));
        btnHistorias.setWidth("320px");

        add(titulo, subtitulo, btnCitas, btnHistorias);
    }
}