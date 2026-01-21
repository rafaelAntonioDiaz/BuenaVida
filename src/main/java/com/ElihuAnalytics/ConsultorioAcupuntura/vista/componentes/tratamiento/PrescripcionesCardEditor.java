package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Prescripcion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
// --- IMPORT NUEVO ---
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.BotonDictado;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent; // Para alineación
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Card de Prescripciones (persistente en BD).
 * Con soporte para dictado de voz en las indicaciones.
 */
public class PrescripcionesCardEditor extends Div {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long historiaId;
    private final HistoriaClinicaService service;
    private final VerticalLayout lista = new VerticalLayout();

    public PrescripcionesCardEditor(Long historiaId, HistoriaClinicaService service) {
        this.historiaId = historiaId;
        this.service = service;

        addClassName("card");
        add(new H3("Prescripciones"));

        Button nuevo = new Button("Nueva prescripción", e -> abrirEditorNuevo());
        add(nuevo);

        lista.setPadding(false);
        lista.setSpacing(false);
        add(lista);

        pintar();
    }

    private void pintar() {
        lista.removeAll();
        List<Prescripcion> pres = service.listarPrescripciones(historiaId);

        for (int i = 0; i < pres.size(); i++) {
            Prescripcion pr = pres.get(i);
            boolean esPrimero = (i == 0);

            String fecha = pr.getFecha() != null ? FECHA.format(pr.getFecha()) : "--/--/---- --:--";
            Paragraph row = new Paragraph(fecha + " · " + Optional.ofNullable(pr.getIndicaciones()).orElse("-"));
            row.addClassName("linea");

            if (esPrimero) {
                Button editar = new Button("Editar", e -> abrirEditorEdicion(pr));
                lista.add(new HorizontalLayout(row, editar));
            } else {
                lista.add(row);
            }
        }

        if (pres.isEmpty()) {
            lista.add(new Paragraph("Sin prescripciones registradas."));
        }
    }

    private void abrirEditorNuevo() {
        TextArea taIndicaciones = new TextArea("Indicaciones");
        taIndicaciones.setWidthFull();
        taIndicaciones.setMinHeight("100px");

        // --- CAMBIO INICIO: Botón de dictado ---
        BotonDictado btnDictar = new BotonDictado(taIndicaciones);

        // Wrapper para alinear el textArea con el botón
        HorizontalLayout wrapper = new HorizontalLayout(taIndicaciones, btnDictar);
        wrapper.setWidthFull();
        wrapper.setAlignItems(FlexComponent.Alignment.END); // Alineado abajo a la derecha
        wrapper.setSpacing(true);
        // --- CAMBIO FIN ---

        TextField tfFrecuencia = new TextField("Frecuencia (opcional)");
        tfFrecuencia.setWidthFull();

        TextField tfDuracion = new TextField("Duración (opcional)");
        tfDuracion.setWidthFull();

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String indic = Optional.ofNullable(taIndicaciones.getValue()).orElse("").trim();
            if (indic.isBlank()) {
                Notification.show("Las indicaciones no pueden estar vacías.");
                return;
            }
            StringBuilder sb = new StringBuilder(indic);
            String freq = Optional.ofNullable(tfFrecuencia.getValue()).orElse("").trim();
            String dur = Optional.ofNullable(tfDuracion.getValue()).orElse("").trim();
            if (!freq.isBlank()) sb.append(" · Frecuencia: ").append(freq);
            if (!dur.isBlank()) sb.append(" · Duración: ").append(dur);

            try {
                service.agregarPrescripcion(historiaId, sb.toString());
                // Removemos el wrapper en lugar de taIndicaciones
                lista.remove(wrapper, tfFrecuencia, tfDuracion, acciones[0]);
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo guardar la prescripción.");
            }
        });

        // Al cancelar, también removemos el wrapper
        Button cancelar = new Button("Cancelar", e -> lista.remove(wrapper, tfFrecuencia, tfDuracion, acciones[0]));
        acciones[0] = new HorizontalLayout(guardar, cancelar);

        // Agregamos el wrapper (con micro) a la lista visual
        lista.add(wrapper, tfFrecuencia, tfDuracion, acciones[0]);
    }

    private void abrirEditorEdicion(Prescripcion pr) {
        TextArea ta = new TextArea("Editar prescripción (más reciente)");
        ta.setWidthFull();
        ta.setMinHeight("120px");
        ta.setValue(Optional.ofNullable(pr.getIndicaciones()).orElse(""));

        // --- CAMBIO INICIO: Botón de dictado ---
        BotonDictado btnDictar = new BotonDictado(ta);

        HorizontalLayout wrapper = new HorizontalLayout(ta, btnDictar);
        wrapper.setWidthFull();
        wrapper.setAlignItems(FlexComponent.Alignment.END);
        wrapper.setSpacing(true);
        // --- CAMBIO FIN ---

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String val = Optional.ofNullable(ta.getValue()).orElse("").trim();
            if (val.isBlank()) {
                Notification.show("Las indicaciones no pueden estar vacías.");
                return;
            }
            try {
                service.actualizarPrescripcion(historiaId, pr.getId(), val);
                lista.remove(wrapper, acciones[0]); // Removemos wrapper
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo actualizar la prescripción.");
            }
        });
        Button cancelar = new Button("Cancelar", e -> lista.remove(wrapper, acciones[0]));
        acciones[0] = new HorizontalLayout(guardar, cancelar);

        lista.add(wrapper, acciones[0]);
    }
}