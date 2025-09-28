package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Recomendacion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Card de Recomendaciones (persistente en BD).
 */
public class RecomendacionesCardEditor extends Div {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Long historiaId;
    private final HistoriaClinicaService service;
    private final VerticalLayout lista = new VerticalLayout();

    public RecomendacionesCardEditor(Long historiaId, HistoriaClinicaService service) {
        this.historiaId = historiaId;
        this.service = service;

        addClassName("card");
        add(new H3("Recomendaciones"));

        Button nuevo = new Button("Nueva recomendación", e -> abrirEditorNuevo());
        add(nuevo);

        lista.setPadding(false);
        lista.setSpacing(false);
        add(lista);

        pintar();
    }

    private void pintar() {
        lista.removeAll();
        List<Recomendacion> recs = service.listarRecomendaciones(historiaId);

        for (int i = 0; i < recs.size(); i++) {
            Recomendacion r = recs.get(i);
            boolean esPrimero = (i == 0);

            String fecha = r.getFecha() != null ? FECHA.format(r.getFecha()) : "--/--/----";
            Paragraph row = new Paragraph(fecha + ": " + Optional.ofNullable(r.getContenido()).orElse("-"));
            row.addClassName("linea");

            if (esPrimero) {
                Button editar = new Button("Editar", e -> abrirEditorEdicion(r));
                lista.add(new HorizontalLayout(row, editar));
            } else {
                lista.add(row);
            }
        }

        if (recs.isEmpty()) {
            lista.add(new Paragraph("Sin recomendaciones previas."));
        }
    }

    private void abrirEditorNuevo() {
        TextArea ta = new TextArea("Nueva recomendación");
        ta.setWidthFull();
        ta.setMinHeight("120px");

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String txt = Optional.ofNullable(ta.getValue()).orElse("").trim();
            if (txt.isBlank()) {
                Notification.show("La recomendación no puede estar vacía.");
                return;
            }
            try {
                service.agregarRecomendacion(historiaId, txt);
                lista.remove(ta, acciones[0]);
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo guardar la recomendación.");
            }
        });
        Button cancelar = new Button("Cancelar", e -> lista.remove(ta, acciones[0]));
        acciones[0] = new HorizontalLayout(guardar, cancelar);
        lista.add(ta, acciones[0]);
    }

    private void abrirEditorEdicion(Recomendacion r) {
        TextArea ta = new TextArea("Editar recomendación (más reciente)");
        ta.setWidthFull();
        ta.setMinHeight("120px");
        ta.setValue(Optional.ofNullable(r.getContenido()).orElse(""));

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String txt = Optional.ofNullable(ta.getValue()).orElse("").trim();
            if (txt.isBlank()) {
                Notification.show("La recomendación no puede estar vacía.");
                return;
            }
            try {
                r.setContenido(txt);
                service.guardar(r.getHistoriaClinica());
                lista.remove(ta, acciones[0]);
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo actualizar la recomendación.");
            }
        });
        Button cancelar = new Button("Cancelar", e -> lista.remove(ta, acciones[0]));
        acciones[0] = new HorizontalLayout(guardar, cancelar);
        lista.add(ta, acciones[0]);
    }
}
