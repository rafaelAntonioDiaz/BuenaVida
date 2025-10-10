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
 * Normaliza la ruta a /pacientes-Uploads/... para recursos estáticos en local y Railway Volumes.
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
        // Normalizar barras: \ -> /
        String r = ruta.replace("\\", "/");

        // Si ya es una URL completa (http:// o https://), devolverla
        if (r.startsWith("http://") || r.startsWith("https://")) {
            return r;
        }

        // Extraer el nombre del archivo (última parte de la ruta)
        int idx = r.lastIndexOf('/');
        String fileName = (idx >= 0 ? r.substring(idx + 1) : r);
        if (fileName.isBlank()) {
            return null;
        }

        // Devolver ruta relativa para recursos estáticos (/pacientes-Uploads/nombreArchivo)
        // Compatible con local (./pacientes-Uploads/) y Railway (/volumes/uploads/pacientes-Uploads/)
        return "/pacientes-Uploads/" + fileName;
    }
}