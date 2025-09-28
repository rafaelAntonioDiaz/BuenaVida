// java
package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.SeguimientoSalud;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Card de Estados de salud (solo lectura para el médico).
 * - Lista los seguimientos (más reciente primero).
 * - Muestra fecha/hora y descripción.
 * - Adjuntos: cada enlace usa la descripción del adjunto (cuando existe) como texto clickable.
 */
public class EstadosSaludResumenCard extends Div {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EstadosSaludResumenCard(HistoriaClinica hc) {
        addClassName("card");
        add(new H3("Estados de salud (seguimientos)"));

        VerticalLayout lista = new VerticalLayout();
        lista.setPadding(false);
        lista.setSpacing(false);
        lista.setWidthFull();

        List<SeguimientoSalud> segs = Optional.ofNullable(hc.getSeguimientos()).orElse(List.of());
        if (segs.isEmpty()) {
            lista.add(new Paragraph("Sin estados de salud registrados."));
            add(lista);
            return;
        }

        // Orden descendente por fecha (nulos al final)
        List<SeguimientoSalud> ordenados = segs.stream()
                .sorted((a, b) -> {
                    var fa = a.getFecha();
                    var fb = b.getFecha();
                    if (fa == null && fb == null) return 0;
                    if (fa == null) return 1;
                    if (fb == null) return -1;
                    return fb.compareTo(fa);
                })
                .collect(Collectors.toList());

        for (SeguimientoSalud seg : ordenados) {
            Div item = new Div();
            item.setWidthFull();
            item.getStyle()
                    .set("padding", "var(--lumo-space-s)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("display", "grid")
                    .set("gap", "var(--lumo-space-s)")
                    .set("min-width", "0");

            String fechaTxt = Optional.ofNullable(seg.getFecha())
                    .map(dt -> dt.format(FECHA_HORA))
                    .orElse("Sin fecha");
            Paragraph head = new Paragraph(fechaTxt);
            head.getStyle().set("font-weight", "600").set("margin", "0");

            Paragraph desc = new Paragraph(Optional.ofNullable(seg.getDescripcion()).orElse("Sin descripción"));
            desc.getStyle().set("margin", "0");

            item.add(head, desc);

            // Adjuntos: preferimos usar las descripcionesPorRuta como texto de enlace
            VerticalLayout adj = new VerticalLayout();
            adj.setPadding(false);
            adj.setSpacing(false);

            Map<String, String> mapa = Optional.ofNullable(seg.getDescripcionesPorRuta()).orElse(Map.of());
            List<String> rutas = Optional.ofNullable(seg.getArchivos()).orElse(List.of());

            if (!mapa.isEmpty()) {
                mapa.forEach((ruta, descripcion) -> {
                    Anchor link = new Anchor(normalizarWebPath(ruta), Optional.ofNullable(descripcion).filter(s -> !s.isBlank()).orElse(nombreArchivo(ruta)));
                    link.setTarget("_blank");
                    adj.add(link);
                });
            } else if (!rutas.isEmpty()) {
                rutas.forEach(ruta -> {
                    Anchor link = new Anchor(normalizarWebPath(ruta), nombreArchivo(ruta));
                    link.setTarget("_blank");
                    adj.add(link);
                });
            } else {
                adj.add(new Paragraph("Sin adjuntos."));
            }

            item.add(adj);
            lista.add(item);
        }

        add(lista);
    }

    private static String normalizarWebPath(String ruta) {
        if (ruta == null || ruta.isBlank()) return "#";
        String r = ruta.replace("\\", "/");
        return r.startsWith("/") ? r : "/" + r;
    }

    private static String nombreArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) return "archivo";
        String base = ruta.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        return (idx >= 0 && idx + 1 < base.length()) ? base.substring(idx + 1) : base;
    }
}