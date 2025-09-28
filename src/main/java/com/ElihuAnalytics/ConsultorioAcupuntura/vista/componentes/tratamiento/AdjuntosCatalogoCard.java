// java
package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.AntecedenteRelevante;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.SeguimientoSalud;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Catálogo de adjuntos reunidos desde antecedentes y seguimientos.
 * (Puedes extenderlo con upload y gestión si lo deseas.)
 */
public class AdjuntosCatalogoCard extends Div {

    public AdjuntosCatalogoCard(HistoriaClinica hc,
                                HistoriaClinicaService service,
                                FileStorageService storage) {
        addClassName("card");
        add(new H3("Adjuntos (catálogo)"));

        List<String> rutas = new ArrayList<>();
        AntecedenteRelevante ant = hc.getAntecedenteRelevante();
        if (ant != null && ant.getRutasArchivos() != null) rutas.addAll(ant.getRutasArchivos());
        List<SeguimientoSalud> segs = Optional.ofNullable(hc.getSeguimientos()).orElse(List.of());
        for (SeguimientoSalud s : segs) {
            if (s.getArchivos() != null) rutas.addAll(s.getArchivos());
        }

        if (rutas.isEmpty()) {
            add(new Paragraph("Sin adjuntos."));
            return;
        }

        VerticalLayout lista = new VerticalLayout();
        lista.setPadding(false);
        lista.setSpacing(false);
        rutas.forEach(r -> {
            String rutaWeb = (r == null ? "" : r.replace("\\", "/"));
            String nombre = nombreArchivo(rutaWeb);
            Anchor link = new Anchor("/" + rutaWeb, nombre);
            link.setTarget("_blank");
            lista.add(link);
        });
        add(lista);
    }

    private static String nombreArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) return "archivo";
        String base = ruta.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        return (idx >= 0 && idx + 1 < base.length()) ? base.substring(idx + 1) : base;
    }
}