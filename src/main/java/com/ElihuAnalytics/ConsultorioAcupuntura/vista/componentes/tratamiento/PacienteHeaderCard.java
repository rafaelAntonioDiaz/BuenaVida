package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Muestra foto (rutaFotoPerfil) y datos básicos del paciente.
 * Normaliza la ruta a /pacientes-uploads/... según tu WebStaticResourcesConfig.
 */
public class PacienteHeaderCard extends Div {

    public PacienteHeaderCard(Paciente p) {
        addClassName("card");
        add(new H3("Paciente"));

        Avatar avatar = new Avatar(p.getNombres() + " " + p.getApellidos());
        avatar.setWidth("72px");
        avatar.setHeight("72px");

        String ruta = p.getRutaFotoPerfil() == null ? "" : p.getRutaFotoPerfil().trim();
        if (!ruta.isBlank()) {
            String url = normalizarRutaFoto(ruta);
            if (url != null) avatar.setImage(url);
        }

        VerticalLayout datos = new VerticalLayout(
                new Text(p.getNombres() + " " + p.getApellidos()),
                new Text("Usuario: " + (p.getUsername() == null ? "-" : p.getUsername())),
                new Text("Celular: " + (p.getCelular() == null ? "-" : p.getCelular()))
        );
        datos.setSpacing(false);
        datos.setPadding(false);

        HorizontalLayout row = new HorizontalLayout(avatar, datos);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setWidthFull();

        add(row);
    }

    private String normalizarRutaFoto(String ruta) {
        String r = ruta.replace("\\", "/");
        if (r.startsWith("http://") || r.startsWith("https://")) return r;
        if (r.contains("pacientes-uploads/")) return r.startsWith("/") ? r : "/" + r;
        int idx = r.lastIndexOf('/');
        String name = (idx >= 0 ? r.substring(idx + 1) : r);
        return name.isBlank() ? null : "/pacientes-uploads/" + name;
    }
}