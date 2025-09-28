package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Recomendacion;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Prescripcion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

/**
 * Card de plan clínico: Diagnóstico tradicional, Recomendaciones y Prescripción.
 * Presentación en tres columnas (solo lectura para el paciente).
 */
public class PlanClinicoCard extends Div {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PlanClinicoCard(HistoriaClinica hc) {
        // Estilo base "card"
        getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        // Contenedor horizontal para 3 columnas
        HorizontalLayout fila = new HorizontalLayout();
        fila.setWidthFull();
        fila.setSpacing(true);
        // Permitir que en pantallas angostas se rompa de línea
        fila.getStyle().set("flex-wrap", "wrap");

        // 1) Diagnóstico tradicional
        VerticalLayout colDiagnostico = new VerticalLayout();
        colDiagnostico.setPadding(false);
        colDiagnostico.setSpacing(false);
        colDiagnostico.getStyle().set("min-width", "280px");

        H3 tituloDx = new H3("Diagnóstico tradicional");
        String textoDx = Optional.ofNullable(hc.getDiagnosticoTradicional())
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse("Sin diagnóstico tradicional registrado.");
        Paragraph pDx = new Paragraph(textoDx);
        colDiagnostico.add(tituloDx, pDx);

        // 2) Recomendaciones (idéntico a la lógica existente, solo lectura)
        VerticalLayout colRecs = new VerticalLayout();
        colRecs.setPadding(false);
        colRecs.setSpacing(false);
        colRecs.getStyle().set("min-width", "280px");

        H3 tituloRecs = new H3("Recomendaciones para mejorar:");
        VerticalLayout listaRecs = new VerticalLayout();
        listaRecs.setPadding(false);
        listaRecs.setSpacing(false);

        if (hc.getRecomendaciones() == null || hc.getRecomendaciones().isEmpty()) {
            listaRecs.add(new Paragraph("Analizando la información que has dado y después de examinarte se te darán tus recomendaciones."));
        } else {
            hc.getRecomendaciones().stream()
                    .sorted(Comparator.comparing(Recomendacion::getFecha).reversed())
                    .forEach(rec -> listaRecs.add(new Paragraph(
                            rec.getFecha().format(FORMATO_FECHA) + ": " + rec.getContenido()
                    )));
        }
        colRecs.add(tituloRecs, listaRecs);

        // 3) Prescripción (solo lectura)
        VerticalLayout colPres = new VerticalLayout();
        colPres.setPadding(false);
        colPres.setSpacing(false);
        colPres.getStyle().set("min-width", "280px");

        H3 tituloPres = new H3("Prescripción");
        VerticalLayout listaPres = new VerticalLayout();
        listaPres.setPadding(false);
        listaPres.setSpacing(false);

        if (hc.getPrescripciones() == null || hc.getPrescripciones().isEmpty()) {
            listaPres.add(new Paragraph("Analizando la información que has dado y después de examinarte, se te darán tus prescripciones."));
        } else {
            hc.getPrescripciones().stream()
                    .sorted(Comparator.comparing(Prescripcion::getFecha).reversed())
                    .forEach(p -> listaPres.add(new Paragraph(
                            p.getFecha().format(FORMATO_FECHA) + ": " + p.getIndicaciones()
                    )));
        }
        colPres.add(tituloPres, listaPres);


        // Igualar crecimiento de las 3 columnas
        fila.add(colDiagnostico, colRecs, colPres);
        fila.setFlexGrow(1, colDiagnostico, colRecs, colPres);
        colDiagnostico.setWidth("0"); // truco para repartir el espacio equitativamente
        colRecs.setWidth("0");
        colPres.setWidth("0");

        add(fila);
    }
}